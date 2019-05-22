package org.fogbowcloud.app.api.http.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.authenticator.models.ApplicationIdentifiers;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.http.HttpWrapper;
import org.fogbowcloud.app.external.ExternalOAuthConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Lazy
@Component
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class);

    @Lazy
    @Autowired
    private IguassuFacade iguassuFacade;

    @Autowired
    private Properties properties;

    public List<OAuthToken> getAll() {
        return this.iguassuFacade.getAllOAuthTokens();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername) throws InvalidParameterException {
        String accessToken = this.iguassuFacade.getAccessTokenByOwnerUsername(ownerUsername);

        if (accessToken != null) {
            return accessToken;
        }

		String messageError = "There is no access token for external file driver for user " + ownerUsername
				+ ". Request failed.";
		LOGGER.error(messageError);
		throw new InvalidParameterException(messageError);
    }

    public void deleteAllTokens() {
        this.iguassuFacade.deleteAllExternalOAuthTokens();
    }

    public OAuthToken requestAccessToken(String authorizationCode, String applicationIdentifiers) throws Exception {
        final String knownClientId = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID);
        final String knownSecret = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET);
        final Gson gson = new Gson();

        final String rawCode = gson.fromJson(authorizationCode, JsonObject.class).get("authorizationCode").getAsString();

        ApplicationIdentifiers applicationIds = gson.fromJson(applicationIdentifiers, ApplicationIdentifiers.class);

        if (applicationIds.getClientId().equals(knownClientId)
                && applicationIds.getSecret().equals(knownSecret)) {
            final String baseUrl = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL);
            final String requestUrl = baseUrl + "?grant_type=authorization_code&code="+rawCode+
                    "&redirect_uri="+applicationIds.getRedirectUri();
            final String authHeadersDecoded = applicationIds.getClientId() + ":" + applicationIds.getSecret();
            final String authHeadersEncoded = Base64.getEncoder().encodeToString(authHeadersDecoded.getBytes());

            List<Header> headers = new LinkedList<>();
            headers.add(new Header() {
                @Override
                public String getName() {
                    return "Authorization";
                }
                @Override
                public String getValue() {
                    return "Basic " + authHeadersEncoded;
                }

                @Override
                public HeaderElement[] getElements() throws ParseException {
                    return new HeaderElement[0];
                }
            });

            try {
                final String oauthTokenRawResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, requestUrl,
                                headers, null);
                if (oauthTokenRawResponse != null) {
                    OAuthToken oAuthToken = gson.fromJson(oauthTokenRawResponse, OAuthToken.class);
                    oAuthToken.updateExpirationDate();
                    storeOAuthToken(oAuthToken);
                    return oAuthToken;
                } else {
                    throw new Exception("You can't use the same authorization code twice.");
                }

            } catch (Exception e) {
                throw new Exception("OAuth Token request failed with message, " + e.getMessage());
            }
        }

        else {
            throw new UnauthorizedRequestException("Your application identifiers are not enable to " +
                    "request an Access Token.");
        }
    }

    private void storeOAuthToken(OAuthToken oAuthToken) {
        User user = this.iguassuFacade.getUser(oAuthToken.getUserId());
        if (user == null) {
            this.iguassuFacade.addUser(oAuthToken.getUserId(), oAuthToken.getAccessToken());
            LOGGER.info("User " + oAuthToken.getUserId() + " was added.");
        }
        this.iguassuFacade.storeOAuthToken(oAuthToken);
    }
}