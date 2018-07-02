package org.fogbowcloud.app.model;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class OAuthToken {

    private static final String JSON_HEADER_ACCESS_TOKEN = "access_token";
    private static final String JSON_HEADER_REFRESH_TOKEN = "refresh_token";
    private static final String JSON_HEADER_USERNAME_OWNER = "username_owner";
    private static final String JSON_HEADER_EXPIRATION_DATE = "expiration_date";

    private String accessToken;
    private String refreshToken;
    private String usernameOwner;
    private Date expirationDate;

    public static final Logger LOGGER = Logger.getLogger(OAuthToken.class);

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

    public void setExpirationDate(long expiresIn) {
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        long expiresInMillisecond = expiresIn * 1000;
        //TODO check if is setting date (and time like hour min sec) correctly
        this.expirationDate = new Date(stamp.getTime() + expiresInMillisecond);
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

}
