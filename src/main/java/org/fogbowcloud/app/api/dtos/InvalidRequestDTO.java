package org.fogbowcloud.app.api.dtos;

public class InvalidRequestDTO {
    private String message;

    public InvalidRequestDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
