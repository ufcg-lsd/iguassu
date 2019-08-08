package org.fogbowcloud.app.core.models.user;

public class RequesterCredential {

    private long userId;
    private String iguassuToken;
    private Integer nonce;

    public RequesterCredential() {}

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequesterCredential that = (RequesterCredential) o;

        if (userId != that.userId) return false;
        if (nonce != that.nonce) return false;
        return iguassuToken.equals(that.iguassuToken);
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + iguassuToken.hashCode();
        result = 31 * result + nonce;
        return result;
    }
}
