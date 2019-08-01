package org.fogbowcloud.app.core.datastore;

import org.fogbowcloud.app.core.auth.models.OAuthToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

import static org.junit.Assert.*;

public class TestOAuthPropertiesKeysTokenDataStore {

    private final static String DATASTORE_URL = "jdbc:h2:/tmp/iguassudb";
    private final static String FAKE_ACCESS_TOKEN = "fake-access-token";
    private final static String FAKE_REFRESH_TOKEN = "fake-refresh-token";
    private final static String FAKE_USER_ID = "fake-user-id";
    private final static Date FAKE_EXPIRATION_DATE = Date.valueOf("3000-01-01");

    private OAuthTokenDataStore datastore;

    @Before
    public void setUp() {
        datastore = new OAuthTokenDataStore(DATASTORE_URL);
    }

    @After
    public void tearDown() {
        this.datastore.deleteAll();
    }

    @Test
    public void testAddToken() {
        OAuthToken token = new OAuthToken(FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, FAKE_USER_ID, FAKE_EXPIRATION_DATE);
        this.datastore.insert(token);
        List<OAuthToken> tokenList = this.datastore.getAll();
        assertEquals(1, tokenList.size());
        assertEquals(token.getAccessToken(), tokenList.get(0).getAccessToken());
    }

    @Test
    public void testUpdateToken() {
        OAuthToken token = new OAuthToken(FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, FAKE_USER_ID, FAKE_EXPIRATION_DATE);
        this.datastore.insert(token);

        String oldAccessToken = token.getAccessToken();
        String newAcessToken = "new-fake-access-token";
        String newRefreshToken = "new-fake-refresh-token";
        Date newExpirationDate = Date.valueOf("1900-01-01");
        token.setAccessToken(newAcessToken);
        token.setRefreshToken(newRefreshToken);
        token.setExpirationDate(newExpirationDate);

        this.datastore.update(oldAccessToken, token);

        List<OAuthToken> tokenList = this.datastore.getAll();
        assertEquals(1, tokenList.size());
        assertEquals(newAcessToken, tokenList.get(0).getAccessToken());
        assertEquals(newRefreshToken, token.getRefreshToken());
        assertEquals(newExpirationDate, token.getExpirationDate());
    }

    @Test
    public void testGetTokenByAccessToken() {
        OAuthToken token = new OAuthToken(FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, FAKE_USER_ID, FAKE_EXPIRATION_DATE);
        this.datastore.insert(token);

        OAuthToken retrievedToken = this.datastore.getTokenByAccessToken(FAKE_ACCESS_TOKEN);
        assertNotNull(retrievedToken);
        assertEquals(token.getRefreshToken(), retrievedToken.getRefreshToken());
        assertEquals(token.getUserId(), retrievedToken.getUserId());
        assertEquals(token.getExpirationDate(), retrievedToken.getExpirationDate());
    }

}