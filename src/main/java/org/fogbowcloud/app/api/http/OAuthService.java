package org.fogbowcloud.app.api.http;

import org.fogbowcloud.app.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.external.oauth.OAuthController;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
public class OAuthService {

    Properties properties;
    OAuthController oAuthController;
    OAuthTokenDataStore oAuthTokenDataStore;

    public OAuthService(Properties properties) {
        this.properties = properties;
        this.oAuthController = new OAuthController(this.properties);
        this.oAuthTokenDataStore = new OAuthTokenDataStore(this.properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL));
    }

    public boolean storeOAuthToken(OAuthToken oAuthToken) {
        boolean saved = this.oAuthTokenDataStore.insert(oAuthToken);
        return saved;
    }

    public List<OAuthToken> getAll() {
        return this.oAuthTokenDataStore.getAll();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername) {
        List<OAuthToken> tokensList = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(ownerUsername);

        // TODO: if list is empty (there is no token for user) throws error to be catched and return appropriate http status code
        for (OAuthToken token: tokensList) {
            if (!token.hasExpired()) {
                return token.getAccessToken();
            }
        }
        // TODO refact to method
        OAuthToken someToken = tokensList.get(0);
        String someRefreshToken = someToken.getRefreshToken();
        OAuthToken newOAuthToken = this.oAuthController.refreshToken(someRefreshToken);
        String accessToken = newOAuthToken.getAccessToken();
        deleteTokens(tokensList);
        System.out.println(getAll().size());
        this.oAuthTokenDataStore.insert(newOAuthToken);

        return accessToken;
    }

    private void deleteTokens(List<OAuthToken> tokenList) {
        for (OAuthToken token: tokenList) {
            this.oAuthTokenDataStore.deleteByAccessToken(token.getAccessToken());
        }
    }

    public void deleteAllTokens() {
        this.oAuthTokenDataStore.deleteAll();
    }
}