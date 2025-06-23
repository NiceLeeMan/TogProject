package org.example.user.registration.mapper;

import org.example.user.registration.dto.SignUpReqDto;
import org.example.user.registration.dto.SignUpResDto;
import org.example.user.common.entity.User;
import org.example.user.common.enums.ErrorCode;

/**
 * 회원가입 요청/응답 DTO ↔ User 엔티티 매핑 전용
 */
public class SignUpMapper {

    private SignUpMapper() {}

    public static User toEntity(SignUpReqDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setStatus(false);
        return user;
    }

    public static SignUpResDto toSuccessResponse(String message) {
        return SignUpResDto.ofSuccess(message);
    }

    public static SignUpResDto toFailureResponse(ErrorCode code, String message) {
        return SignUpResDto.ofFailure(code, message);
    }
}