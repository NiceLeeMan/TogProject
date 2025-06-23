package org.example.user.common.enums;

// 1) 에러/비즈니스 코드 enum
public enum ErrorCode {
    SUCCESS(0),           // 정상 처리
    DUPLICATE(1001),      // 중복 가입
    INVALID_INPUT(1002),
    INVALID_CREDENTIAL(1003),// 인증(아이디/비밀번호) 실패
    SERVER_ERROR(2001); // 서버 내부 오류


    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    /**
     * 외부에서 숫자 코드를 꺼내 쓸 때
     */
    public int getCode() {
        return code;
    }
}
