package org.example.friend.dto;




public class AddFriendReqDto {
    private String username;
    private String friendUsername;  // 기존의 friendId(String) 대신 username

    public AddFriendReqDto() { }

    public AddFriendReqDto(String username, String friendUsername) {
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