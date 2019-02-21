package org.fogbowcloud.app.external;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.models.OAuthToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Properties;

public class ExternalOAuthController {

    private Properties properties;

    private final Logger LOGGER = Logger.getLogger(ExternalOAuthController.class);

    public ExternalOAuthController(Properties properties) {
        this.properties = properties;
    }

    public OAuthToken refreshToken(String refreshToken) {
        String server_url = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL);
        String request_query = server_url + "?grant_type=refresh_token" + "&refresh_token=" + refreshToken;
        String clientId = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID);
        String clientSecret = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET);

        OAuthToken refreshedToken = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(request_query);
            String encoding = Base64.getEncoder().encodeToString((clientId +":" + clientSecret).getBytes("UTF-8"));
            request.setHeader("Authorization", "Basic " + encoding);
            request.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(request);
            String responseJsonString = EntityUtils.toString(response.getEntity());
            JSONObject responseJson = new JSONObject(responseJsonString);

            String jsonHeaderAccessToken = "access_token";
            String jsonHeaderExpiresIn = "expires_in";
            String jsonHeaderRefreshToken = "refresh_token";
            String jsonHeaderUserId = "user_id";

            String expiresIn = responseJson.optString(jsonHeaderExpiresIn);
            Timestamp stamp = new Timestamp(System.currentTimeMillis());
            long expiresInMillisecond = Long.valueOf(expiresIn) * 1000;
            Date expirationDate = new Date(stamp.getTime() + expiresInMillisecond);

            refreshedToken = new OAuthToken(
                    responseJson.optString(jsonHeaderAccessToken),
                    responseJson.optString(jsonHeaderRefreshToken),
                    responseJson.optString(jsonHeaderUserId),
                    expirationDate);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return refreshedToken;
    }

    public boolean userExists(String username) {
        String user_provisioning_url = this.properties.getProperty(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_USERS_URL);
        String request_query = user_provisioning_url + "?search=" + username;
        String fileDriverAdminUsername = this.properties.getProperty(ExternalOAuthConstants.STORAGE_SERVICE_ADMIN_USERNAME);
        String fileDriverAdminPassword = this.properties.getProperty(ExternalOAuthConstants.STORAGE_SERVICE_ADMIN_PASSWORD);

        String statusCodeOk = "100";
        boolean hasUser = false;
        String resSatusCode = "";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(request_query);

            String encoding = Base64.getEncoder().encodeToString((fileDriverAdminUsername + ":" + fileDriverAdminPassword).getBytes("UTF-8"));
            request.setHeader("Authorization", "Basic " + encoding);

            HttpResponse response = client.execute(request);
            String responseXMLString = EntityUtils.toString(response.getEntity());

            JSONObject responseJsonObj = XML.toJSONObject(responseXMLString);
            resSatusCode = responseJsonObj.getJSONObject("ocs")
                    .getJSONObject("meta")
                    .optString("statuscode");
            hasUser = !responseJsonObj.getJSONObject("ocs")
                    .getJSONObject("data")
                    .optString("users")
                    .isEmpty();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return resSatusCode.equals(statusCodeOk) && hasUser;
    }

}
