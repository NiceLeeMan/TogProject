package org.example.user.authentication.service;

import org.example.user.common.dao.UserDAO;
import org.example.user.common.entity.User;

import java.sql.SQLException;

/**
 * 세션/상태 변경만을 담당
 */
public class SessionManager {
    private final UserDAO userDAO;

    public SessionManager(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    /**
     * 로그인 성공 후 상태 업데이트
     *
     * @param user 인증된 사용자
     * @throws SQLException DB 오류 시
     */
    public void createSession(User user) throws SQLException {
        userDAO.updateStatusById(user.getId(), true);
    }
}