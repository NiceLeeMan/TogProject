package org.example.chat.dto;

// SignUpResDto.java
public class SignUpResDto {
    private String message;

    // 기본 생성자
    public SignUpResDto() { }

    // 전체 필드를 받는 생성자
    public SignUpResDto(String message) {
        this.message = message;
    }

    /* ===== Getter ===== */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SignUpResDto{" +
                "message='" + message + '\'' +
                '}';
    }
}
