package org.fogbowcloud.app.core.models.auth;

import com.google.gson.annotations.SerializedName;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * An instance of this class is used to represent the OAuth2 authentication response and the
 * security information user to communicate with the Storage Service.
 */
public class OAuthToken implements Serializable {
    private static final long INITIAL_VERSION = 0L;
    private static final String USER_ID = "user_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRES_IN = "expires_in";
    private static final String EXPIRATION_DATE = "expiration_date";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * An Access Token is used to make requests that can manipulate data in the Storage Service.
     */
    @SerializedName(ACCESS_TOKEN)
    @Column(name = ACCESS_TOKEN)
    private String accessToken;

    /**
     * A Refresh Token is used to update the expired access token.
     */
    @SerializedName(REFRESH_TOKEN)
    @Column(name = REFRESH_TOKEN)
    private String refreshToken;

    /**
     * The OAuth2 user identifier.
     */
    @SerializedName(USER_ID)
    @Column(name = USER_ID)
    private String userId;

    /**
     * The time of expiration of the Access Token in milliseconds.
     */
    @SerializedName(EXPIRES_IN)
    @Column(name = EXPIRES_IN)
    private int expirationTime;

    /**
     * All OAuthTokens have an associated version. This is useful because access token usage is
     * distributed, so some control over them is required.
     */
    @Column
    private long version;

    /**
     * The time of expiration encapsulated in a Date object.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = EXPIRATION_DATE)
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

    public long getId() {
        return id;
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
