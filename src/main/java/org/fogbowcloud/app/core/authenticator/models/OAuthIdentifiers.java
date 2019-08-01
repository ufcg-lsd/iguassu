package org.fogbowcloud.app.core.authenticator.models;

import java.util.Objects;

public class OAuthIdentifiers {
    private String clientId;
    private String secret;
    private String redirectUri;

    public OAuthIdentifiers(String clientId, String secret, String redirectUri) {
        this.clientId = clientId;
        this.secret = secret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthIdentifiers that = (OAuthIdentifiers) o;
        return clientId.equals(that.clientId) &&
                secret.equals(that.secret) &&
                Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, secret, redirectUri);
    }
}