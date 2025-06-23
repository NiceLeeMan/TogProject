package org.example.user.registration.dto;

import org.example.user.common.enums.ErrorCode;

// SignUpResDto.java
public class SignUpResDto {
    private boolean success;       // 처리 성공 여부
    private ErrorCode code;        // 비즈니스/에러 코드
    private String message;        // 사용자용 설명 메시지

    // 기본 생성자 (Jackson 등 직렬화 라이브러리용)
    public SignUpResDto() {}

    // 전체 필드 생성자
    public SignUpResDto(boolean success, ErrorCode code, String message) {
        this.success = success;
        this.code    = code;
        this.message = message;
    }

    /**
     * 성공 응답 팩토리
     * @param message 성공 메시지
     * @return SignUpResDto
     */
    public static SignUpResDto ofSuccess(String message) {
        return new SignUpResDto(true, ErrorCode.SUCCESS, message);
    }

    /**
     * 실패 응답 팩토리
     * @param code 에러 코드
     * @param message 실패 메시지
     * @return SignUpResDto
     */
    public static SignUpResDto ofFailure(ErrorCode code, String message) {
        return new SignUpResDto(false, code, message);
    }

    // --- getters & setters ---
    public boolean isSuccess() {
        return success;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}