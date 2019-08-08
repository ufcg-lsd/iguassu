package org.fogbowcloud.app.api.dtos;

import com.google.gson.annotations.SerializedName;
import org.fogbowcloud.app.core.models.user.Credential;

import java.util.Objects;

public class CredentialDTO {

    private long id;

    @SerializedName("iguassu_token")
    private String iguassuToken;

    private int nonce;

    public CredentialDTO(Credential credential) {
        this.id = credential.getId();
        this.iguassuToken = credential.getIguassuToken();
        this.nonce = credential.getNonce();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIguassuToken() {
        return iguassuToken;
    }

    public void setIguassuToken(String iguassuToken) {
        this.iguassuToken = iguassuToken;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialDTO that = (CredentialDTO) o;
        return id == that.id &&
                iguassuToken.equals(that.iguassuToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, iguassuToken);
    }
}
