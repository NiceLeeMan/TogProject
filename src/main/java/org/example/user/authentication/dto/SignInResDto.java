package org.example.user.authentication.dto;

import org.example.user.common.enums.ErrorCode;

// SignInResDto.java
public class SignInResDto {
    private boolean success;
    private ErrorCode error;
    private String message;
    private UserInfo userInfo;

    // 기본 생성자
    public SignInResDto() {
    }
    // 전체 필드를 받는 생성자
    public SignInResDto(boolean success, ErrorCode error, String message, UserInfo userInfo) {
        this.success = success;
        this.error = error;
        this.message = message;
        this.userInfo = userInfo;
    }
    /**
     * 성공 응답 팩토리
     * @param userInfo 로그인된 사용자 정보
     */
    public static SignInResDto ofSuccess(UserInfo userInfo) {
        return new SignInResDto(true, null, null, userInfo);
    }

    /**
     * 실패 응답 팩토리
     * @param error   에러 코드
     * @param message 사용자에게 보여줄 메시지
     */
    public static SignInResDto ofFailure(ErrorCode error, String message) {
        return new SignInResDto(false, error, message, null);
    }

    // getter & setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorCode getError() {
        return error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
