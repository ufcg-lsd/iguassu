package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.ArrebolController;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class OAuthService {

    @Lazy
    @Autowired
    ArrebolController arrebolController;

    private static final Logger LOGGER = Logger.getLogger(OAuthService.class);

    public void storeOAuthToken(OAuthToken oAuthToken) {
        User user = this.arrebolController.getUser(oAuthToken.getUsernameOwner());
        if (user == null) {
            this.arrebolController.addUser(oAuthToken.getUsernameOwner(), oAuthToken.getAccessToken());
        }
        this.arrebolController.storeOAuthToken(oAuthToken);
    }

    public List<OAuthToken> getAll() {
        return this.arrebolController.getAllOAuthTokens();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername) throws InvalidParameterException {
        String accessToken = this.arrebolController.getAccessTokenByOwnerUsername(ownerUsername);

        if (accessToken != null) {
            return accessToken;
        }

		String messageError = "There is no access token for external file driver for user " + ownerUsername
				+ ". Request failed.";
		LOGGER.error(messageError);
		throw new InvalidParameterException(messageError);
    }

    public void deleteAllTokens() {
        this.arrebolController.deleteAllExternalOAuthTokens();
    }
}