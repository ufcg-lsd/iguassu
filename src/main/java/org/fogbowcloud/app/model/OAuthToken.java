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
    private final String usernameOwner;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getUsernameOwner() {
        return usernameOwner;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public static OAuthToken fromJSON(JSONObject tokenJson) {
        LOGGER.info("Reading Token from JSON");
        List<Task> tasks = new ArrayList<>();

        String dateStr = tokenJson.optString(JSON_HEADER_EXPIRATION_DATE);
        Date expirationDate = Date.valueOf(dateStr);

        OAuthToken token = new OAuthToken(
                tokenJson.optString(JSON_HEADER_ACCESS_TOKEN),
                tokenJson.optString(JSON_HEADER_REFRESH_TOKEN),
                tokenJson.optString(JSON_HEADER_USERNAME_OWNER),
                expirationDate);

        LOGGER.debug("Job read from JSON is from owner: " + token.getUsernameOwner());
        return token;
    }

}
