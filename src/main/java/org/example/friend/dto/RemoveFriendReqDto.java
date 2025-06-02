package org.example.friend.dto;


public class RemoveFriendReqDto {
    private String username;
    private String friendUsername;

    public RemoveFriendReqDto() { }

    public RemoveFriendReqDto(String username, String friendUsername) {
        this.username = username;
        this.friendUsername  = friendUsername;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriendUsername() {
        return friendUsername;
    }
    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }
}