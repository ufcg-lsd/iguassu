package org.fogbowcloud.app.api.dtos;

import io.swagger.annotations.ApiModelProperty;
import org.fogbowcloud.app.core.models.user.User;

import java.io.Serializable;
import java.util.Objects;

/** Data transfer object projection */
public class UserDTO implements Serializable {

    @ApiModelProperty(notes = "The User ID", position = 1, example = "1")
    private Long id;

    @ApiModelProperty(notes = "The Alias of User", position = 2, example = "iguassu-admin")
    private String alias;

    @ApiModelProperty(notes = "The User Credentials", position = 3, dataType = "CredentialDTO")
    private CredentialDTO credentials;

    public UserDTO(User user) {
        this.id = user.getId();
        this.alias = user.getAlias();
        this.credentials = new CredentialDTO(user.getCredentials());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public CredentialDTO getCredentials() {
        return credentials;
    }

    public void setCredentials(CredentialDTO credentials) {
        this.credentials = credentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return id == userDTO.id &&
                alias.equals(userDTO.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, alias);
    }
}
