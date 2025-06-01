package org.example.user.dto;

// SignInResDto.java
public class SignInResDto {
    private Long id;
    private String userId;
    private String name;

    // 기본 생성자
    public SignInResDto() { }

    // 전체 필드를 받는 생성자
    public SignInResDto(Long id, String userId, String name) {
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    /* ===== Getter ===== */
    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SignInResDto{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
