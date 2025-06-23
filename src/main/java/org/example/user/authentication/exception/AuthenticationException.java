package org.example.user.authentication.exception;

/**
 * 인증 실패 전용 예외
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}