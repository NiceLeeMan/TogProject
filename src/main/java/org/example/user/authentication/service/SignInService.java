package org.example.user.authentication.service;

import org.example.user.authentication.dto.SignInReqDto;
import org.example.user.authentication.dto.SignInResDto;
import org.example.user.common.entity.User;
import org.example.user.common.enums.ErrorCode;
import org.example.user.authentication.mapper.SignInMapper;

import org.example.user.authentication.exception.AuthenticationException;

import java.sql.SQLException;

/**
 * 로그인 전용 서비스
 */
/**
 * 로그인 절차 오케스트레이션
 * - authenticate(), createSession(), mapper.toSuccessResponse()/toFailureResponse() 호출만!
 */
public class SignInService {

    private final Authenticator authenticator;
    private final SessionManager sessionManager;

    public SignInService(
            Authenticator authenticator,
            SessionManager sessionManager
    ) {
        this.authenticator   = authenticator;
        this.sessionManager  = sessionManager;
    }

    public SignInResDto signIn(SignInReqDto reqDto) {

        try {
            User user = authenticator.authenticate(reqDto.getUsername(), reqDto.getPassword());
            sessionManager.createSession(user);
            System.out.println("성공");
            return SignInMapper.toSuccessResponse(user);

        } catch (AuthenticationException ae) {
            // 인증 실패 매핑
            return SignInMapper.toFailureResponse(
                    ErrorCode.INVALID_CREDENTIAL,
                    ae.getMessage()
            );
        } catch (SQLException se) {
            // DB 오류 매핑
            se.printStackTrace();
            return SignInMapper.toFailureResponse(
                    ErrorCode.SERVER_ERROR,
                    "서버 오류로 로그인에 실패했습니다."
            );
        }
    }
}