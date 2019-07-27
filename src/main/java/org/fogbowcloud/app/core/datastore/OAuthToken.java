package org.fogbowcloud.app.core.datastore;

import com.google.gson.annotations.SerializedName;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.JsonKey;
import org.json.JSONObject;

public class OAuthToken {

    private static final Logger logger = Logger.getLogger(OAuthToken.class);

    private static final long INITIAL_VERSION = 0L;
    private static final String USER_ID = "user_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRES_IN = "expires_in";

    @SerializedName(ACCESS_TOKEN)
    private String accessToken;

    @SerializedName(REFRESH_TOKEN)
    private String refreshToken;

    @SerializedName(USER_ID)
    private String userId;

    @SerializedName(EXPIRES_IN)
    private int expirationTime;

    private long version;
    private Date expirationDate;

    public OAuthToken() {}

    OAuthToken(String accessToken, String refreshToken, String userId, Date expirationDate) {
        this.version = INITIAL_VERSION;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.expirationTime = 3600;
    }

    private OAuthToken(
            String accessToken,
            String refreshToken,
            String userId,
            Date expirationDate,
            long version) {
        this.version = version;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.expirationTime = 3600;
    }

    public static OAuthToken fromJSON(JSONObject tokenJson) {
        logger.info("Reading Token from JSON");

        long dateStr = tokenJson.getLong(EXPIRES_IN);
        Date expirationDate = new Date(dateStr);

        OAuthToken token =
                new OAuthToken(
                        tokenJson.optString(ACCESS_TOKEN),
                        tokenJson.optString(REFRESH_TOKEN),
                        tokenJson.optString(USER_ID),
                        expirationDate,
                        tokenJson.getLong(JsonKey.TOKEN_VERSION.getKey()));

        logger.debug("Job read from JSON is from user: " + token.getUserId());
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

    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long secondsToExpires) {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        long expiresInMillisecond = secondsToExpires * 1000;
        this.expirationDate = new Date(stamp.getTime() + expiresInMillisecond);
    }

    void setExpirationDate(Date expirationDate) {

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
        return version == that.version
                && expirationTime == that.expirationTime
                && accessToken.equals(that.accessToken)
                && refreshToken.equals(that.refreshToken)
                && userId.equals(that.userId)
                && expirationDate.equals(that.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                version, accessToken, refreshToken, userId, expirationTime, expirationDate);
    }
}
