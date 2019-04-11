package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class OAuthService {

    @Lazy
    @Autowired
    IguassuFacade iguassuFacade;

    private static final Logger LOGGER = Logger.getLogger(OAuthService.class);

    public void storeOAuthToken(OAuthToken oAuthToken) {
        User user = this.iguassuFacade.getUser(oAuthToken.getUsernameOwner());
        if (user == null) {
            this.iguassuFacade.addUser(oAuthToken.getUsernameOwner(), oAuthToken.getAccessToken());
            LOGGER.info("User " + oAuthToken.getUsernameOwner() + " was added.");
        }
        this.iguassuFacade.storeOAuthToken(oAuthToken);
    }

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
}