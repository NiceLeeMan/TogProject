package org.example.friend.dto;



public class GetFriendsListReqDto {
    private String username;

    public GetFriendsListReqDto() { }

    public GetFriendsListReqDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
