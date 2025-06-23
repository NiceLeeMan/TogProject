package org.example.user.authentication.mapper;

import org.example.user.authentication.dto.SignInResDto;
import org.example.user.authentication.dto.UserInfo;
import org.example.user.common.entity.User;
import org.example.user.common.enums.ErrorCode;

/**
 * SignIn 응답 DTO ↔ User 엔티티 매핑 전용
 */
public class SignInMapper {

    private SignInMapper() { /* 인스턴스 생성 방지 */ }

    /**
     * 로그인 성공 결과 → SignInResDto 변환
     */
    public static SignInResDto toSuccessResponse(User user) {
        UserInfo info = new UserInfo(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getProfileUrl()
        );
        return SignInResDto.ofSuccess(info);
    }

    /**
     * 로그인 실패 결과 → SignInResDto 변환
     */
    public static SignInResDto toFailureResponse(ErrorCode code, String message) {
        return SignInResDto.ofFailure(code, message);
    }
}