package org.fogbowcloud.app.external.oauth;

import org.apache.http.util.EntityUtils;
import org.fogbowcloud.app.model.OAuthToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Properties;

// TODO change class name
public class OAuthController {

    private Properties properties;

    public OAuthController(Properties properties) {
        this.properties = properties;
    }

    public OAuthToken refreshToken(String refreshToken) {
        String server_url = this.properties.getProperty(OAuthConstants.SERVER_TOKEN_URL);
        String request_query = server_url + "?grant_type=refresh_token" + "&refresh_token=" + refreshToken;
        String clientId = this.properties.getProperty(OAuthConstants.MY_CLIENT_ID);
        String clientSecret = this.properties.getProperty(OAuthConstants.MY_SECRET_ID);

        OAuthToken refreshedToken = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(request_query);
            String encoding = Base64.getEncoder().encodeToString((clientId +":" + clientSecret).getBytes("UTF-8"));
            request.setHeader("Authorization", "Basic " + encoding);
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

}
