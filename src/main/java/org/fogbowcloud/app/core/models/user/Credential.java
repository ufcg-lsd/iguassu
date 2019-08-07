package org.fogbowcloud.app.core.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;
import org.fogbowcloud.app.utils.TokenEncrypt;

import javax.persistence.*;

/** This class encapsulates the user security information. */
@Entity
@Table(name = "credential")
public class Credential {
    private static final String IGUASSU_TOKEN_COLUMN_NAME = "iguassu_token";
    private static final String OAUTH_TOKEN_COLUMN_NAME = "oauth_token";
    private static final String OAUTH_TOKEN_ID_COLUMN_NAME = "oauth_token_id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = IGUASSU_TOKEN_COLUMN_NAME)
    @Convert(converter = TokenEncrypt.class)
    @SerializedName(IGUASSU_TOKEN_COLUMN_NAME)
    private String iguassuToken;

    @JsonIgnore
    @Column(name = OAUTH_TOKEN_COLUMN_NAME)
    @JoinColumn(name = OAUTH_TOKEN_ID_COLUMN_NAME)
    private OAuthToken oauthToken;

    @JsonIgnore
    @Transient
    private Integer nonce;

    public Credential(String iguassuToken, Integer nonce, OAuthToken oauthToken) {
        this.iguassuToken = iguassuToken;
        this.nonce = nonce;
        this.oauthToken = oauthToken;
    }

    public String getIguassuToken() {
        return iguassuToken;
    }

    public void setIguassuToken(String iguassuToken) {
        this.iguassuToken = iguassuToken;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public long getId() {
        return id;
    }

    public OAuthToken getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(OAuthToken oauthToken) {
        this.oauthToken = oauthToken;
    }
}
