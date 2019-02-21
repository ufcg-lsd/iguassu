package org.fogbowcloud.app.core.models;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;

public class OAuthToken {

    private static final String JSON_HEADER_ACCESS_TOKEN = "access_token";
    private static final String JSON_HEADER_REFRESH_TOKEN = "refresh_token";
    private static final String JSON_HEADER_USERNAME_OWNER = "token_owner_username";
    private static final String JSON_HEADER_EXPIRATION_DATE = "expiration_date";

    private String accessToken;
    private String refreshToken;
    private String usernameOwner;
    private Date expirationDate;

    private static final Logger LOGGER = Logger.getLogger(OAuthToken.class);

    public OAuthToken() {

    }

    public static OAuthToken fromJSON(JSONObject tokenJson) {
        LOGGER.info("Reading Token from JSON");

        long dateStr = tokenJson.getLong(JSON_HEADER_EXPIRATION_DATE);
        Date expirationDate = new Date(dateStr);

        OAuthToken token = new OAuthToken(
                tokenJson.optString(JSON_HEADER_ACCESS_TOKEN),
                tokenJson.optString(JSON_HEADER_REFRESH_TOKEN),
                tokenJson.optString(JSON_HEADER_USERNAME_OWNER),
                expirationDate);

        LOGGER.debug("Job read from JSON is from owner: " + token.getUsernameOwner());
        return token;
    }

    public boolean hasExpired() {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date currentDate = new Date(stamp.getTime());
        return currentDate.getTime() > this.expirationDate.getTime();
    }

    public OAuthToken(String accessToken, String refreshToken, String usernameOwner, Date expirationDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.usernameOwner = usernameOwner;
        this.expirationDate = expirationDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsernameOwner() {
        return usernameOwner;
    }

    public void setUsernameOwner(String usernameOwner) {
        this.usernameOwner = usernameOwner;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long secondsToExpires) {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        long expiresInMillisecond = secondsToExpires * 1000;
        this.expirationDate = new Date(stamp.getTime() + expiresInMillisecond);
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

}
