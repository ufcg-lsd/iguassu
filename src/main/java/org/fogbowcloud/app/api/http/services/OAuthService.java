package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.ArrebolController;
import org.fogbowcloud.app.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.external.oauth.OAuthController;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Lazy
@Component
public class OAuthService {

    @Lazy
    @Autowired
    ArrebolController arrebolController;

    public boolean storeOAuthToken(OAuthToken oAuthToken) {
        return this.arrebolController.storeOAuthToken(oAuthToken);
    }

    public List<OAuthToken> getAll() {
        return this.arrebolController.getAllOAuthTokens();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername) {
        List<OAuthToken> tokensList = this.arrebolController.getAccessTokensByOwnerUsername(ownerUsername);

        for (OAuthToken token: tokensList) {
            if (!token.hasExpired()) {
                return token.getAccessToken();
            }
        }
        // TODO refact to method
        OAuthToken someToken = tokensList.get(0);
        String someRefreshToken = someToken.getRefreshToken();
        OAuthToken newOAuthToken = this.arrebolController.refreshExternalOAuthToken(someRefreshToken);
        String accessToken = newOAuthToken.getAccessToken();
        deleteTokens(tokensList);
        this.arrebolController.storeOAuthToken(newOAuthToken);

        return accessToken;
    }

    private void deleteTokens(List<OAuthToken> tokenList) {
        for (OAuthToken token: tokenList) {
            this.arrebolController.deleteOAuthTokenByAcessToken(token.getAccessToken());
        }
    }

    public void deleteAllTokens() {
        this.arrebolController.deleteAllExternalOAuthTokens();
    }
}