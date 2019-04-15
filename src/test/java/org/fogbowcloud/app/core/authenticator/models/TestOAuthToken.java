package org.fogbowcloud.app.core.authenticator.models;

import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.junit.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class TestOAuthToken {

    @Test
    public void hasExpired() throws InterruptedException {
        OAuthToken oAuthToken = new OAuthToken();
        long expiresInOneSecond = 1;
        oAuthToken.setExpirationDate(expiresInOneSecond);
        TimeUnit.SECONDS.sleep(2);
        assertTrue(oAuthToken.hasExpired());
    }
}