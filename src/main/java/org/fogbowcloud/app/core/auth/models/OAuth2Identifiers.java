package org.fogbowcloud.app.core.auth.models;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * An instance of this class is used to represent a client application that is authorized to
 * authenticate or made any other request in Iguassu. If a client application does not have the
 * correct information defined by this structure then such application client will not be allowed to
 * authenticate to the Iguassu and therefore make future requests.
 */
public class OAuth2Identifiers {

    /** This property is provided by the OAuth2 of the Storage Service; must be non-null. */
    @SerializedName("client_id")
    private String clientAppId;

    /** This property is provided by the OAuth2 of the Storage Service; must be non-null. */
    private String secret;

    /** This property is provided by the OAuth2 of the Storage Service; must be non-null. */
    @SerializedName("redirect_uri")
    private String redirectUri;

    /**
     * @param clientAppId A hash that represents an identifier of the application that is authorized
     *     to made requests.
     * @param secret A hash that represents a secret key of the application authorized.
     * @param redirectUri An URI that represents the host address that is authorized to
     *     authenticate.
     */
    public OAuth2Identifiers(String clientAppId, String secret, String redirectUri) {
        this.clientAppId = clientAppId;
        this.secret = secret;
        this.redirectUri = redirectUri;
    }

    /** Returns a raw hash that represents the OAuth2 authorized application. */
    public String getClientAppId() {
        return clientAppId;
    }

    /** Returns a raw hash that represents the secret of the OAuth2 authorized application. */
    public String getSecret() {
        return secret;
    }

    /** Returns a redirect URI that represents the OAuth2 authorized application. */
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OAuth2Identifiers that = (OAuth2Identifiers) o;
        return clientAppId.equals(that.clientAppId)
                && secret.equals(that.secret)
                && Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientAppId, secret, redirectUri);
    }
}
