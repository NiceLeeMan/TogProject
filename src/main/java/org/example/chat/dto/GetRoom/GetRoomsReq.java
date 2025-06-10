package org.example.chat.dto.GetRoom;

public class GetRoomsReq {
    private String username;

    public GetRoomsReq() {}

    public GetRoomsReq(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}