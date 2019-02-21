package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.models.OAuthToken;
import org.fogbowcloud.app.core.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class OAuthService {

    @Lazy
    @Autowired
    IguassuController iguassuController;

    private static final Logger LOGGER = Logger.getLogger(OAuthService.class);

    public void storeOAuthToken(OAuthToken oAuthToken) {
        User user = this.iguassuController.getUser(oAuthToken.getUsernameOwner());
        if (user == null) {
            this.iguassuController.addUser(oAuthToken.getUsernameOwner(), oAuthToken.getAccessToken());
        }
        this.iguassuController.storeOAuthToken(oAuthToken);
    }

    public List<OAuthToken> getAll() {
        return this.iguassuController.getAllOAuthTokens();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername) throws InvalidParameterException {
        String accessToken = this.iguassuController.getAccessTokenByOwnerUsername(ownerUsername);

        if (accessToken != null) {
            return accessToken;
        }

		String messageError = "There is no access token for external file driver for user " + ownerUsername
				+ ". Request failed.";
		LOGGER.error(messageError);
		throw new InvalidParameterException(messageError);
    }

    public void deleteAllTokens() {
        this.iguassuController.deleteAllExternalOAuthTokens();
    }
}