package org.fogbowcloud.app.api.http.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.models.user.OAuth2Identifiers;
import org.fogbowcloud.app.core.models.user.OAuthToken;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.constants.JsonKey;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Properties;

@Lazy
@Component
public class AuthService {

    private static final Logger logger = Logger.getLogger(AuthService.class);

    @Lazy @Autowired private IguassuFacade iguassuFacade;

    @Lazy @Autowired private Properties properties;

    public User authenticate(String authorizationCode, String applicationIdentifiers)
            throws Exception {

        final Gson gson = new Gson();

        final String rawAuthorizationCode =
                gson.fromJson(authorizationCode, JsonObject.class)
                        .get(JsonKey.AUTHORIZATION_CODE.getKey())
                        .getAsString();

        final OAuth2Identifiers applicationIds =
                gson.fromJson(applicationIdentifiers, OAuth2Identifiers.class);

        if (isAReliableApp(applicationIds)) {
            try {
                return this.iguassuFacade.authenticateUser(applicationIds, rawAuthorizationCode);
            } catch (Exception e) {
                throw new GeneralSecurityException();
            }

        } else {
            throw new UnauthorizedRequestException(
                    "Your application identifiers are not enable to request an Access Token.");
        }
    }

    public User authorizeUser(String userCredentials) throws UnauthorizedRequestException {
        User user;
        try {
            user = this.iguassuFacade.authorizeUser(userCredentials);
            user.resetSession();
            this.iguassuFacade.updateUser(user);
            logger.info("Retrieving user " + user.getAlias());
        } catch (GeneralSecurityException e) {
            logger.error("Error while trying authorize", e);
            throw new UnauthorizedRequestException(
                    "There was an error trying to authenticate.\nTry again later.");
        } catch (NullPointerException e) {
            logger.error("Incorrect credentials! Try login again.");
            throw new UnauthorizedRequestException("Incorrect credentials! Try login again.");
        }
        return user;
    }

    public String refreshToken(String userAlias, Long version) throws Exception {
        OAuthToken oAuthToken = this.iguassuFacade.findUserOAuthTokenByAlias(userAlias);
        if (Objects.isNull(oAuthToken)) {
            throw new UnauthorizedRequestException("Was not found token for user [" + userAlias + "]");
        }
        if (oAuthToken.getVersion() > version) {
            if (oAuthToken.hasExpired()) {
                return refreshAndDelete(oAuthToken).getAccessToken();
            } else {
                return oAuthToken.getAccessToken();
            }
        } else if (oAuthToken.getVersion() == version) {
            return refreshAndDelete(oAuthToken).getAccessToken();
        } else {
            throw new IllegalArgumentException("Invalid version");
        }
    }

    private OAuthToken refreshAndDelete(OAuthToken oAuthToken) throws GeneralSecurityException {
        OAuthToken refreshedToken;
        try {
            refreshedToken = this.iguassuFacade.refreshToken(oAuthToken);
        } catch (GeneralSecurityException gse) {
            throw new GeneralSecurityException(gse.getMessage());
        }
//        this.iguassuFacade.deleteOAuthToken(oAuthToken);
//        this.iguassuFacade.storeOAuthToken(refreshedToken);
        return refreshedToken;
    }

    private boolean isAReliableApp(OAuth2Identifiers applicationIds) {
        final String knownAppClientId =
                this.properties.getProperty(ConfProperty.OAUTH_STORAGE_SERVICE_CLIENT_ID.getProp());
        final String knownSecret =
                this.properties.getProperty(
                        ConfProperty.OAUTH_STORAGE_SERVICE_CLIENT_SECRET.getProp());

        return Objects.nonNull(applicationIds.getClientAppId())
                && Objects.nonNull(applicationIds.getSecret())
                && applicationIds.getClientAppId().equals(knownAppClientId)
                && applicationIds.getSecret().equals(knownSecret);
    }
}
