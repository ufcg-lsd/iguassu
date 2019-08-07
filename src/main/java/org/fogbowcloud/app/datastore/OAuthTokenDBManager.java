package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.auth.OAuthToken;
import org.fogbowcloud.app.datastore.repositories.OAuthTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OAuthTokenDBManager implements DBManager<OAuthToken> {
    private static final Logger logger = Logger.getLogger(OAuthTokenDBManager.class);

    private static OAuthTokenDBManager instance;

    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;

    private OAuthTokenDBManager() {
    }

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
    public OAuthToken findOne(long id) {
        return this.oAuthTokenRepository.findById(id).isPresent() ? this.oAuthTokenRepository.findById(id).get() : null;
    }

    public OAuthToken findByUserId(String userId) {
        return this.oAuthTokenRepository.findByUserId(userId);
    }

    public List<OAuthToken> findAll() {
        return this.oAuthTokenRepository.findAll();
    }

    @Override
    public void update(OAuthToken oAuthToken) {
        this.oAuthTokenRepository.deleteById(oAuthToken.getId());
        this.oAuthTokenRepository.save(oAuthToken);
    }

    @Override
    public void delete(long id) {
        this.oAuthTokenRepository.deleteById(id);
    }
}
