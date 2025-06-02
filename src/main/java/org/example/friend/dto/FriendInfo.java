package org.example.friend.dto;



public class FriendInfo {
    private Long userId;
    private String username;   // 필요하다면 응답에 username도 추가 가능
    private String name;
    private String profileImgUrl;


    public FriendInfo() { }

    public FriendInfo(Long userId, String name, String username, String profileImgUrl) {
        this.userId = userId;
        this.name          = name;
        this.username      = username;
        this.profileImgUrl = profileImgUrl;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;

    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }
    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }
}
