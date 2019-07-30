package org.fogbowcloud.app.core.auth;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.fogbowcloud.app.api.exceptions.StorageServiceConnectException;
import org.fogbowcloud.app.core.auth.models.OAuth2Identifiers;
import org.fogbowcloud.app.core.auth.models.OAuthToken;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.exceptions.InvalidAuthorizationCodeException;
import org.fogbowcloud.app.utils.HttpWrapper;

import java.util.*;

/**
 * A utility class that encapsulates communication logic, that is, requests, with the Storage
 * Service.
 */
class AuthRequestsHelper {

    private Properties properties;

    /** Default constructor. */
    AuthRequestsHelper(Properties properties) {
        this.properties = properties;
    }

    /**
     * Requests a new {@link OAuthToken#getAccessToken()}. This is the authentication operation.
     *
     * @param oAuth2Identifiers is the client application information.
     * @param authorizationCode the code
     * @return an {@link OAuthToken} with your information updated.
     */
    OAuthToken getToken(OAuth2Identifiers oAuth2Identifiers, String authorizationCode) {
        final String baseUrl =
                this.properties.getProperty(ConfProperty.OAUTH_STORAGE_SERVICE_TOKEN_URL.getProp());
        final String requestUrl =
                baseUrl
                        + "?grant_type=authorization_code&code="
                        + authorizationCode
                        + "&redirect_uri="
                        + oAuth2Identifiers.getRedirectUri();

        List<Header> headers = new LinkedList<>();
        mountsHeaders(
                headers,
                encodeHeaders(oAuth2Identifiers.getClientAppId(), oAuth2Identifiers.getSecret()));

        return requestAccessToken(requestUrl, headers, new Gson());
    }

    /**
     * Requests a new {@link OAuthToken#getAccessToken()} using the old {@link
     * OAuthToken#getRefreshToken()}.
     *
     * @param oAuthToken an old {@link OAuthToken#getAccessToken()} that will be used to update it
     *     current tokens.
     * @return a new {@link OAuthToken#getAccessToken()}. *
     */
    OAuthToken refreshToken(OAuthToken oAuthToken) {
        final Gson gson = new Gson();
        String refreshToken = oAuthToken.getRefreshToken();
        final String baseUrl =
                this.properties.getProperty(ConfProperty.OAUTH_STORAGE_SERVICE_TOKEN_URL.getProp());
        final String requestUrl =
                baseUrl + "?grant_type=refresh_token&refresh_token=" + refreshToken;

        List<Header> headers = new LinkedList<>();
        String clientId =
                this.properties.getProperty(ConfProperty.OAUTH_STORAGE_SERVICE_CLIENT_ID.getProp());
        String secret =
                this.properties.getProperty(
                        ConfProperty.OAUTH_STORAGE_SERVICE_CLIENT_SECRET.getProp());

        mountsHeaders(headers, encodeHeaders(clientId, secret));

        try {
            final String oAuthTokenRawResponse =
                    HttpWrapper.doRequest(HttpPost.METHOD_NAME, requestUrl, headers, null);
            OAuthToken refreshOAuthToken = gson.fromJson(oAuthTokenRawResponse, OAuthToken.class);
            refreshOAuthToken.updateExpirationDate();
            refreshOAuthToken.setVersion(oAuthToken.getVersion() + 1);
            return refreshOAuthToken;
        } catch (HttpHostConnectException e) {
            throw new StorageServiceConnectException(
                    "Failed connect to Storage Service: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while trying to refresh OAuth2 tokens with cause: " + e.getMessage());
        }
    }

    private OAuthToken requestAccessToken(String requestUrl, List<Header> headers, Gson gson) {
        try {
            final String oAuthTokenRawResponse =
                    HttpWrapper.doRequest(HttpPost.METHOD_NAME, requestUrl, headers, null);

            if (oAuthTokenRawResponse != null) {
                OAuthToken oAuthToken = gson.fromJson(oAuthTokenRawResponse, OAuthToken.class);
                oAuthToken.updateExpirationDate();
                return oAuthToken;
            } else {
                throw new InvalidAuthorizationCodeException();
            }

        } catch (HttpHostConnectException e) {
            throw new StorageServiceConnectException(
                    "Failed connect to Storage Service: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("OAuthToken request failed with message, " + e.getMessage());
        }
    }

    private String encodeHeaders(String clientId, String secret) {
        final String authHeadersDecoded = clientId + ":" + secret;
        return Objects.requireNonNull(
                Base64.getEncoder().encodeToString(authHeadersDecoded.getBytes()));
    }

    private void mountsHeaders(List<Header> headers, String authHeadersEncoded) {
        headers.add(
                new Header() {
                    @Override
                    public String getName() {
                        return "Authorization";
                    }

                    @Override
                    public String getValue() {
                        return "Basic " + authHeadersEncoded;
                    }

                    @Override
                    public HeaderElement[] getElements() throws ParseException {
                        return new HeaderElement[0];
                    }
                });
    }
}
