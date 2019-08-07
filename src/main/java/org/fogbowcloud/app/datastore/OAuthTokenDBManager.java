package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.auth.OAuthToken;
import org.fogbowcloud.app.datastore.repositories.OAuthTokenRepository;
import org.fogbowcloud.app.datastore.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class OAuthTokenDBManager implements DBManager<OAuthToken> {
    private static final Logger logger = Logger.getLogger(OAuthTokenDBManager.class);

    private static OAuthTokenDBManager instance;

    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;

    private OAuthTokenDBManager() {}

    public synchronized static OAuthTokenDBManager getInstance() {
        if (instance == null) {
            instance = new OAuthTokenDBManager();
        }
        return instance;
    }


    @Override
    public void save(OAuthToken oAuthToken) {
        this.oAuthTokenRepository.save(oAuthToken);
    }

    @Override
    public OAuthToken retrieveById(String id) {
        return null;
    }

    @Override
    public void update(OAuthToken oAuthToken) {

    }
}
