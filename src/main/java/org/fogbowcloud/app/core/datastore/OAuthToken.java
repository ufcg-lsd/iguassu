package org.fogbowcloud.app.core.datastore;

import com.google.gson.annotations.SerializedName;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.OAuth;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;

public class OAuthToken {

    @SerializedName(OAuth.ACCESS_TOKEN_JSON_KEY)
    private String accessToken;

    @SerializedName(OAuth.REFRESH_TOKEN_JSON_KEY)
    private String refreshToken;

    @SerializedName(OAuth.USER_ID_JSON_KEY)
    private String userId;

    @SerializedName(OAuth.EXPIRES_IN_JSON_KEY)
    private int expirationTime;

    private Date expirationDate;

    private static final Logger LOGGER = Logger.getLogger(OAuthToken.class);

    public OAuthToken() { }

    public OAuthToken(String accessToken, String refreshToken, String userId, Date expirationDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.expirationTime = 3600;
    }

    public static OAuthToken fromJSON(JSONObject tokenJson) {
        LOGGER.info("Reading Token from JSON");

        long dateStr = tokenJson.getLong(OAuth.EXPIRES_IN_JSON_KEY);
        Date expirationDate = new Date(dateStr);

        OAuthToken token = new OAuthToken(
                tokenJson.optString(OAuth.ACCESS_TOKEN_JSON_KEY),
                tokenJson.optString(OAuth.REFRESH_TOKEN_JSON_KEY),
                tokenJson.optString(OAuth.USER_ID_JSON_KEY),
                expirationDate);

        LOGGER.debug("Job read from JSON is from owner: " + token.getUserId());
        return token;
    }

    public boolean hasExpired() {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date currentDate = new Date(stamp.getTime());
        return currentDate.getTime() > this.expirationDate.getTime();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long secondsToExpires) {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        long expiresInMillisecond = secondsToExpires * 1000;
        this.expirationDate = new Date(stamp.getTime() + expiresInMillisecond);
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void updateExpirationDate() {
        Timestamp stamp = new Timestamp(System.currentTimeMillis() + (this.expirationTime*1000));
        this.expirationDate = new Date(stamp.getTime());
    }

    public void setExpirationDate(Date expirationDate) {

        this.expirationDate = expirationDate;
    }

}
