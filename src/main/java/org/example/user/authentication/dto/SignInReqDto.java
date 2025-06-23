package org.example.user.authentication.dto;

// SignInReqDto.java
public class SignInReqDto {
    private String username;
    private String password;

    // 기본 생성자
    public SignInReqDto() { }

    // 전체 필드를 받는 생성자
    public SignInReqDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /* ===== Getter ===== */
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "SignInReqDto{" +
                "userId='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
