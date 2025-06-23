package org.example.user.authentication.service;

import org.example.user.common.dao.UserDAO;
import org.example.user.common.entity.User;
import org.example.user.authentication.exception.AuthenticationException;

import java.sql.SQLException;

/**
 * 인증만을 담당
 */
public class Authenticator {
    private final UserDAO userDAO;

    public Authenticator(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 사용자 자격 증명 검증
     *
     * @param username 입력받은 아이디
     * @param password 입력받은 비밀번호
     * @return User 객체 (인증 성공 시)
     * @throws AuthenticationException 인증 실패 시
     * @throws SQLException            DB 오류 시
     */
    public User authenticate(String username, String password)
            throws AuthenticationException, SQLException {
        User user = userDAO.SignInUser(username);
        if (user == null) {
            throw new AuthenticationException("사용자를 찾을 수 없습니다.");
        }
        // 실제로는 해시 검증
        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("비밀번호가 올바르지 않습니다.");
        }
        return user;
    }
}