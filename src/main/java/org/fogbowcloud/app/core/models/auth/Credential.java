package org.fogbowcloud.app.core.models.auth;

/** This class encapsulates the user security information. */
public class Credential {

    private final String userId;
    private String iguassuToken;
    private Integer nonce;

    public Credential(String userId, String iguassuToken, Integer nonce) {
        this.userId = userId;
        this.iguassuToken = iguassuToken;
        this.nonce = nonce;
    }

    public String getUserId() {
        return userId;
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
}
