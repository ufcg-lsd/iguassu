package org.fogbowcloud.app.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.fogbowcloud.app.core.models.user.Credential;

import java.io.Serializable;
import java.util.Objects;

/** Data transfer object projection */
public class CredentialDTO implements Serializable {

    @ApiModelProperty(notes = "The Iguassu Token of User", position = 1, example = "iguassu-token")
    @JsonProperty("iguassu_token")
    private String iguassuToken;

    @ApiModelProperty(notes = "A nonce is an arbitrary number that can only be used once", position = 2, dataType = "int", example = "100000")
    private int nonce;

    CredentialDTO(Credential credential) {

        this.iguassuToken = credential.getIguassuToken();
        this.nonce = credential.getNonce();
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
        return iguassuToken.equals(that.iguassuToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iguassuToken);
    }
}
