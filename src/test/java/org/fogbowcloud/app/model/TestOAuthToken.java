package org.fogbowcloud.app.model;

import org.junit.Test;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TestOAuthToken {

    private final String DATASTORE_URL = "jdbc:h2:/tmp/iguassudb";
    private final String FAKE_ACCESS_TOKEN = "fake-access-token";
    private final String FAKE_REFRESH_TOKEN = "fake-refresh-token";
    private final String FAKE_OWNER_USERNAME = "fake-owner-username";
    private final Date FAKE_EXPIRATION_DATE = Date.valueOf("3000-01-01");

    @Test
    public void hasExpired() throws InterruptedException {
        OAuthToken oAuthToken = new OAuthToken();
        long expiresInOneSecond = 1;
        oAuthToken.setExpirationDate(expiresInOneSecond);
        TimeUnit.SECONDS.sleep(2);
        assertTrue(oAuthToken.hasExpired());
    }
}