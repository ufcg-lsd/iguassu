package org.fogbowcloud.app.api.dtos;

import org.fogbowcloud.app.core.models.user.User;

import java.util.Objects;

public class UserDTO {

    private Long id;
    private String alias;
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
