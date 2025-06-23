package org.example.user.authentication.dto;

public class UserInfo {
    private Long id;
    private String username;
    private String name;
    private String profileUrl;

    // 기본 생성자 (Jackson 등 직렬화 라이브러리용)
    public UserInfo() { }

    // 전체 필드를 받는 생성자
    public UserInfo(Long id, String username, String name, String profileUrl) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.profileUrl = profileUrl;
    }

    // --- getters & setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
