package org.fogbowcloud.app.api.http.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.exceptions.NotFoundAccessToken;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.authenticator.models.OAuthIdentifiers;
import org.fogbowcloud.app.core.authenticator.models.RandomString;
import org.fogbowcloud.app.core.dto.AuthDTO;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.http.HttpWrapper;
import org.fogbowcloud.app.external.ExternalOAuthConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.*;

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

    public AuthDTO authenticateWithOAuth2(String authorizationCode, String applicationIdentifiers) throws Exception {
        final String knownClientId = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID);
        final String knownSecret = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET);
        final Gson gson = new Gson();

        final String rawCode = gson.fromJson(authorizationCode, JsonObject.class).get("authorizationCode").getAsString();

        OAuthIdentifiers applicationIds = gson.fromJson(applicationIdentifiers, OAuthIdentifiers.class);

        if (applicationIds.getClientId().equals(knownClientId)
                && applicationIds.getSecret().equals(knownSecret)) {
            final String baseUrl = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL);
            final String requestUrl = baseUrl + "?grant_type=authorization_code&code="+rawCode+
                    "&redirect_uri="+applicationIds.getRedirectUri();
            final String authHeadersDecoded = applicationIds.getClientId() + ":" + applicationIds.getSecret();
            final String authHeadersEncoded = Base64.getEncoder().encodeToString(authHeadersDecoded.getBytes());

            List<Header> headers = new LinkedList<>();
            mountsHeaders(headers, authHeadersEncoded);

            return requestOAuthAccessToken(requestUrl, headers, gson);

        } else {
            throw new UnauthorizedRequestException("Your application identifiers are not enable to " +
                    "request an Access Token.");
        }
    }

    private AuthDTO requestOAuthAccessToken(String requestUrl, List<Header> headers, Gson gson) throws Exception {
        try {
            final String oAuthTokenRawResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, requestUrl,
                    headers, null);
            if (oAuthTokenRawResponse != null) {
                OAuthToken oAuthToken = gson.fromJson(oAuthTokenRawResponse, OAuthToken.class);
                oAuthToken.updateExpirationDate();

                final String iguassuToken = this.generateIguassuToken(oAuthToken.getUserId());
                this.storeOAuthToken(oAuthToken, iguassuToken);

                return new AuthDTO(oAuthToken.getUserId(), iguassuToken);
            } else {
                throw new Exception("You can't use the same authorization code twice.");
            }

        } catch (Exception e) {
            throw new Exception("OAuth Token request failed with message, " + e.getMessage());
        }
    }

    private void mountsHeaders(List<Header> headers, String authHeadersEncoded) {
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
    }

    private void storeOAuthToken(OAuthToken oAuthToken, String iguassuToken) {
        User user = this.iguassuFacade.getUser(oAuthToken.getUserId());
        if (user == null) {
            this.iguassuFacade.addUser(oAuthToken.getUserId(), iguassuToken);
            LOGGER.info("OAuth2 tokens for the user " + oAuthToken.getUserId() + " was stored.");
        }
        this.iguassuFacade.storeOAuthToken(oAuthToken);
    }

    private String generateIguassuToken(String userId) {
        final String sessionToken = new RandomString(21, userId).nextString();

        return Base64.getEncoder().encodeToString(sessionToken.getBytes());
    }

    public String refreshToken(String accessToken) throws Exception {
        OAuthToken oAuthToken = this.iguassuFacade.getTokenByAccessToken(accessToken);
        List<OAuthToken> tokens = this.iguassuFacade.getAllTokenByUserName(oAuthToken.getUserId());
        if(Objects.nonNull(oAuthToken) && tokens.contains(oAuthToken) && oAuthToken.hasExpired()){
            for(OAuthToken o : tokens){
                if(o.getVersion() > oAuthToken.getVersion()){
                    return o.getAccessToken();
                }
            }
            OAuthToken refreshedToken = refreshToken(oAuthToken);
            final String iguassuToken = this.generateIguassuToken(refreshedToken.getUserId());
            this.storeOAuthToken(refreshedToken, iguassuToken);

            final long oldVersions = oAuthToken.getVersion() - 1;
            if(oldVersions >= 0){
                this.iguassuFacade.removeOAuthTokens(oAuthToken.getUserId(), oldVersions);
            }
            return refreshedToken.getAccessToken();
        } else {
            throw new NotFoundAccessToken("The accessToken[" + accessToken +"] was not found");
        }
    }

    private OAuthToken refreshToken(OAuthToken oAuthToken) throws Exception {
        final Gson gson = new Gson();
        String refreshToken = oAuthToken.getRefreshToken();
        final String baseUrl = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL);
        final String requestUrl = baseUrl + "?grant_type=refresh_token&refresh_token=" + refreshToken;

        List<Header> headers = new LinkedList<>();
        mountsAuthorizationHeader(headers);

        try {
            final String oAuthTokenRawResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, requestUrl, headers, null);
            OAuthToken refreshOAuthToken = gson.fromJson(oAuthTokenRawResponse, OAuthToken.class);
            refreshOAuthToken.updateExpirationDate();
            refreshOAuthToken.setVersion(oAuthToken.getVersion() + 1);
            return refreshOAuthToken;
        } catch (Exception e){
            throw new Exception("error while refreshing the token");
        }
    }

    private void mountsAuthorizationHeader(List<Header> headers){
        String clientId = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID);
        String clientSecret = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET);

        final String authHeadersDecoded = clientId + ":" + clientSecret;
        final String authHeadersEncoded = Base64.getEncoder().encodeToString(authHeadersDecoded.getBytes());

        mountsHeaders(headers, authHeadersEncoded);
    }

}