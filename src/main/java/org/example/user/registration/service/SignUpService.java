package org.example.user.registration.service;

import org.example.user.common.dao.UserDAO;
import org.example.user.registration.dto.SignUpReqDto;
import org.example.user.registration.dto.SignUpResDto;
import org.example.user.common.entity.User;
import org.example.user.common.enums.ErrorCode;
import org.example.user.registration.mapper.SignUpMapper;

import java.sql.SQLException;

/**
 * 회원가입 전용 서비스
 */
public class SignUpService {

    private final UserDAO userDAO;

    public SignUpService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 회원가입 처리
     *
     * @param reqDto SignUpReqDto
     * @return SignUpResDto
     */
    public SignUpResDto signUp(SignUpReqDto reqDto) {
        try {
            // 1) 중복 체크
            if (userDAO.existsByUserId(reqDto.getUsername())) {
                return SignUpResDto.ofFailure(
                        ErrorCode.DUPLICATE,
                        "이미 존재하는 사용자 ID 입니다."
                );
            }

            // 3) DB 저장
            User newUser = SignUpMapper.toEntity(reqDto);
            Long id = userDAO.registerUser(newUser);
            if (id == null) {
                return SignUpResDto.ofFailure(
                        ErrorCode.SERVER_ERROR,
                        "회원가입에 실패했습니다. 다시 시도해주세요."
                );
            }

            // 4) 성공
            return SignUpMapper.toSuccessResponse("회원가입이 성공적으로 완료되었습니다.");

        } catch (SQLException e) {
            e.printStackTrace();
            return SignUpMapper.toFailureResponse(
                    ErrorCode.SERVER_ERROR,
                    "서버 오류로 인해 회원가입에 실패했습니다."
            );
        }
    }
}