package org.fogbowcloud.app.core.auth.models;

import java.util.Objects;

/**
 * An instance of this class is used to represent a client application that is authorized to authenticate or made any
 * other request in Iguassu. If a client application does not have the correct information defined by this structure
 * then such application client will not be allowed to authenticate to the Iguassu and therefore make future requests.
 */
public class OAuthIdentifiers {

    /**
     * Client Application identifier representation provides by OAuth2.
     */
    private String clientAppId;

    /**
     * Client Application secret key representation provides by OAuth2.
     */
    private String secret;

    /**
     * Redirect URI representation provides by OAuth2.
     */
    private String redirectUri;

    /**
     * @param clientAppId
     * @param secret
     * @param redirectUri
     */
    public OAuthIdentifiers(String clientAppId, String secret, String redirectUri) {
        this.clientAppId = clientAppId;
        this.secret = secret;
        this.redirectUri = redirectUri;
    }

    public String getClientAppId() {
        return clientAppId;
    }

    public void setClientAppId(String clientAppId) {
        this.clientAppId = clientAppId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OAuthIdentifiers that = (OAuthIdentifiers) o;
        return clientAppId.equals(that.clientAppId) &&
                secret.equals(that.secret) &&
                Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientAppId, secret, redirectUri);
    }
}
