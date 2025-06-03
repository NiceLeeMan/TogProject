package org.example.user.service;




import org.example.user.dao.UserDAO;
import org.example.user.dto.SignInReqDto;
import org.example.user.dto.SignInResDto;
import org.example.user.dto.SignUpReqDto;
import org.example.user.dto.SignUpResDto;
import org.example.user.entity.User;

import java.sql.SQLException;

/**
 * UserService: DAO 레이어를 호출하여 비즈니스 로직 처리
 *
 * - 회원가입(signUp), 로그인(signIn), 로그아웃(signOut) 메서드를 구현
 * - DAO 메서드만 호출하고, 비즈니스 로직(중복 체크, 비밀번호 확인 등)을 수행
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 회원가입
     *
     * 1) 이미 존재하는 userId인지 확인
     * 2) 중복이 아니면 User 객체를 만들어 DAO.save() 호출
     * 3) 생성된 ID가 null이면 실패, 그렇지 않으면 성공 메시지 반환
     *
     * @param reqDto SignUpReqDto
     * @return SignUpResDto
     */
    public SignUpResDto signUp(SignUpReqDto reqDto) {
        System.out.println(">>> UserService.signUp() 시작: " + reqDto.getUsername());
        try {
            // 1) 중복 체크
            boolean exists = userDAO.existsByUserId(reqDto.getUsername());
            if (exists) {
                return new SignUpResDto("이미 존재하는 사용자 ID 입니다.");
            }

            // 2) User 엔티티 생성
            User newUser = new User();
            newUser.setName(reqDto.getName());
            newUser.setUsername(reqDto.getUsername());
            newUser.setPassword(reqDto.getPassword());
            newUser.setStatus(false); // 가입 시 디폴트 오프라인

            // 3) DB 저장
            Long generatedId = userDAO.registerUser(newUser);
            if (generatedId == null) {
                return new SignUpResDto("회원가입에 실패했습니다. 다시 시도해주세요.");
            }

            return new SignUpResDto("회원가입이 성공적으로 완료되었습니다.");

        } catch (SQLException e) {
            e.printStackTrace();
            return new SignUpResDto("서버 오류로 인해 회원가입에 실패했습니다.");
        }
    }

    /**
     * 로그인
     *
     * 1) userId로 User 조회
     * 2) 사용자 존재하지 않으면 null 반환
     * 3) 비밀번호 비교 (단순 문자열 비교 예시)
     * 4) 로그인 성공 시 status=true로 업데이트 후, SignInResDto 반환
     *
     * @param reqDto SignInReqDto
     * @return SignInResDto (성공) 또는 null (실패)
     */
    public SignInResDto signIn(SignInReqDto reqDto) {
        try {
            // 1) 사용자 조회
            User user = userDAO.findByUserId(reqDto.getUsername());
            if (user == null) {
                return null; // 사용자 없음
            }

            // 2) 비밀번호 비교 (실제 서비스에서는 해시 검증을 해야 함)
            if (!user.getPassword().equals(reqDto.getPassword())) {
                return null; // 비밀번호 불일치
            }

            // 3) 로그인 성공: status = true로 변경
            userDAO.updateStatusById(user.getId(), true);

            // 4) 응답 DTO 생성
            return new SignInResDto(user.getId(), user.getUsername(), user.getName(), user.getProfileUrl());

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 로그아웃
     *
     * 1) userId로 User 조회
     * 2) 사용자 존재하지 않으면 false
     * 3) status = false로 바꾼 뒤 true 반환
     *
     * @param username 로그인 ID
     * @return 로그아웃 성공 여부
     */
    public boolean signOut(String username) {
        try {
            User user = userDAO.findByUserId(username);
            if (user == null) {
                return false;
            }
            int updated = userDAO.updateStatusById(user.getId(), false);
            return updated == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

