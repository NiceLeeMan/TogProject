package org.example.user.dto;

// SignInReqDto.java
public class SignInReqDto {
    private String userId;
    private String password;

    // 기본 생성자
    public SignInReqDto() { }

    // 전체 필드를 받는 생성자
    public SignInReqDto(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    /* ===== Getter ===== */
    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "SignInReqDto{" +
                "userId='" + userId + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
