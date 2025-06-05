package org.example.user.dto;


/**
 * 로그아웃 응답 DTO
 * 성공 시: { "message": "로그아웃 성공" }
 * 실패 시: { "message": "로그아웃에 실패했습니다." }
 */
public class SignOutResDto {
    private String message;

    public SignOutResDto() { }

    public SignOutResDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
