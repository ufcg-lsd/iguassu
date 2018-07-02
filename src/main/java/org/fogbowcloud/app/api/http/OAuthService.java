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
    
}