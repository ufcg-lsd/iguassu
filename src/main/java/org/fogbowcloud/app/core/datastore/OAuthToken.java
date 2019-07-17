package org.fogbowcloud.app.core.datastore;

import com.google.gson.annotations.SerializedName;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.OAuthPropertiesKeys;
import org.json.JSONObject;

public class OAuthToken {

    private static final long INITIAL_VERSION = 0L;
    private static final Logger LOGGER = Logger.getLogger(OAuthToken.class);
    @SerializedName(OAuthPropertiesKeys.TOKEN_VERSION_JSON_KEY)
    private long version;
    @SerializedName(OAuthPropertiesKeys.ACCESS_TOKEN_JSON_KEY)
    private String accessToken;
    @SerializedName(OAuthPropertiesKeys.REFRESH_TOKEN_JSON_KEY)
    private String refreshToken;
    @SerializedName(OAuthPropertiesKeys.USER_ID_JSON_KEY)
    private String userId;
    @SerializedName(OAuthPropertiesKeys.EXPIRES_IN_JSON_KEY)
    private int expirationTime;
    private Date expirationDate;

    public OAuthToken() {
    }

    public OAuthToken(String accessToken, String refreshToken, String userId, Date expirationDate) {
        this.version = INITIAL_VERSION;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.expirationTime = 3600;
    }

    public OAuthToken(String accessToken, String refreshToken, String userId, Date expirationDate,
        long version) {
        this.version = version;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.expirationTime = 3600;
    }

    public static OAuthToken fromJSON(JSONObject tokenJson) {
        LOGGER.info("Reading Token from JSON");

        long dateStr = tokenJson.getLong(OAuthPropertiesKeys.EXPIRES_IN_JSON_KEY);
        Date expirationDate = new Date(dateStr);

        OAuthToken token = new OAuthToken(
            tokenJson.optString(OAuthPropertiesKeys.ACCESS_TOKEN_JSON_KEY),
            tokenJson.optString(OAuthPropertiesKeys.REFRESH_TOKEN_JSON_KEY),
            tokenJson.optString(OAuthPropertiesKeys.USER_ID_JSON_KEY),
            expirationDate,
            tokenJson.getLong(OAuthPropertiesKeys.TOKEN_VERSION_JSON_KEY));

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

    public void setExpirationDate(Date expirationDate) {

        this.expirationDate = expirationDate;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void updateExpirationDate() {
        Timestamp stamp = new Timestamp(System.currentTimeMillis() + (this.expirationTime * 1000));
        this.expirationDate = new Date(stamp.getTime());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OAuthToken that = (OAuthToken) o;
        return version == that.version &&
            expirationTime == that.expirationTime &&
            accessToken.equals(that.accessToken) &&
            refreshToken.equals(that.refreshToken) &&
            userId.equals(that.userId) &&
            expirationDate.equals(that.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(version, accessToken, refreshToken, userId, expirationTime, expirationDate);
    }
}
