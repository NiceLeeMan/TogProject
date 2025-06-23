package org.example.user.session.service;


import org.example.user.common.dao.UserDAO;

import java.sql.SQLException;

/**
 * 로그아웃 전용 서비스
 */
public class SignOutService {

    private final UserDAO userDAO;

    public SignOutService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    /**
     * 로그아웃 처리: userId의 status를 false(오프라인)로 업데이트
     *
     * @param userId 로그아웃할 사용자 PK
     * @return 업데이트 성공 시 true, 실패 시 false
     */
    public boolean signOut(Long userId) {
        try {
            int updatedRows = userDAO.updateStatusById(userId, false);
            return updatedRows == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}