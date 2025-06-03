package org.example.user.dto;

// SignInResDto.java
public class SignInResDto {
    private Long id;
    private String username;
    private String profileUrl;
    private String name;

    // 기본 생성자
    public SignInResDto() { }

    // 전체 필드를 받는 생성자
    public SignInResDto(Long id, String userId, String name, String profileUrl) {
        this.id = id;
        this.username = userId;
        this.name = name;
        this.profileUrl = profileUrl;
    }

    /* ===== Getter ===== */
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public String toString() {
        return "SignInResDto{" +
                "id=" + id +
                ", userId='" + username + '\'' +
                ", name='" + name + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                '}';
    }
}
