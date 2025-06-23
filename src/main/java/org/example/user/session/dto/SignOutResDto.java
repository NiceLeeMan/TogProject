package org.example.user.session.dto;


import org.example.user.common.enums.ErrorCode;

/**
 * 로그아웃 응답 DTO
 * - success: 처리 성공 여부
 * - code: 비즈니스/에러 코드
 * - message: 사용자용 설명 메시지
 */
public class SignOutResDto {
    private boolean success;
    private ErrorCode code;
    private String message;

    // 기본 생성자 (직렬화/역직렬화용)
    public SignOutResDto() { }

    // 전체 필드 생성자
    public SignOutResDto(boolean success, ErrorCode code, String message) {
        this.success = success;
        this.code    = code;
        this.message = message;
    }

    /**
     * 성공 응답 팩토리
     * @param message 성공 메시지
     */
    public static SignOutResDto ofSuccess(String message) {
        return new SignOutResDto(true, ErrorCode.SUCCESS, message);
    }

    /**
     * 실패 응답 팩토리
     * @param code    에러 코드
     * @param message 실패 메시지
     */
    public static SignOutResDto ofFailure(ErrorCode code, String message) {
        return new SignOutResDto(false, code, message);
    }

    // --- getters & setters ---

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}