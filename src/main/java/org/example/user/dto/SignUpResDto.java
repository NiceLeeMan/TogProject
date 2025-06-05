package org.example.user.dto;

// SignUpResDto.java
public class SignUpResDto {
    private boolean success;
    private String message;


    // 기본 생성자
    public SignUpResDto() { }

    // 전체 필드를 받는 생성자
    public SignUpResDto(String message) {
        this.message = message;
        this.success = true;
    }

    /* ===== Getter ===== */
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SignUpResDto{" +
                "message='" + message + '\'' +
                '}';
    }
}
