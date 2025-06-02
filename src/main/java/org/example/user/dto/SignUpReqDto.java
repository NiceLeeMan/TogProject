package org.example.user.dto;

// SignUpReqDto.java
public class SignUpReqDto {
    private String name;
    private String username;
    private String password;

    // 기본 생성자 (Jackson, Gson 등 JSON 매핑 라이브러리용)
    public SignUpReqDto() { }

    // 전체 필드를 받는 생성자
    public SignUpReqDto(String name, String userId, String password) {
        this.name = name;
        this.username = userId;
        this.password = password;
    }

    /* ===== Getter ===== */
    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "SignUpReqDto{" +
                "name='" + name + '\'' +
                ", userId='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
