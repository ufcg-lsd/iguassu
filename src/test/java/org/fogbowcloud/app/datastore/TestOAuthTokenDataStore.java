package org.fogbowcloud.app.datastore;

import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.blowout.core.model.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestOAuthTokenDataStore {

    private final static String DATASTORE_URL = "jdbc:h2:/tmp/iguassudb";
    private final static String FAKE_ACCESS_TOKEN = "fake-access-token";
    private final static String FAKE_REFRESH_TOKEN = "fake-refresh-token";
    private final static String FAKE_OWNER_USERNAME = "fake-owner-username";
    private final static Date FAKE_EXPIRATION_DATE = Date.valueOf("3000-01-01");

    private OAuthTokenDataStore datastore;

    @Before
    public void setUp() {
        datastore = new OAuthTokenDataStore(DATASTORE_URL);
    }

    @After
    public void tearDown() {
        //this.datastore.deleteAll();
    }

    @Test
    public void testAddToken() {
        OAuthToken token = new OAuthToken(FAKE_ACCESS_TOKEN, FAKE_REFRESH_TOKEN, FAKE_OWNER_USERNAME, FAKE_EXPIRATION_DATE);
        this.datastore.insert(token);
        List<OAuthToken> tokenList = this.datastore.getAll();
        assertEquals(1, tokenList.size());
        assertEquals(token.getAccessToken(), tokenList.get(0).getAccessToken());
    }

}