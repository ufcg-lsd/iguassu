package org.fogbowcloud.app.core.dto;

import com.google.gson.annotations.SerializedName;

public class AuthDTO {

    @SerializedName("iguassu_token")
    private String iguassuToken;

    @SerializedName("user_id")
    private String userId;

    public AuthDTO(String userId, String iguassuToken) {
        this.iguassuToken = iguassuToken;
        this.userId = userId;
    }

    public String getIguassuToken() {
        return iguassuToken;
    }

    public void setIguassuToken(String iguassuToken) {
        this.iguassuToken = iguassuToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthDTO that = (AuthDTO) o;

        if (!iguassuToken.equals(that.iguassuToken)) return false;
        return userId.equals(that.userId);

    }

    @Override
    public int hashCode() {
        int result = iguassuToken.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}