package org.example.user.dao;




import javax.sql.DataSource;
import org.example.user.entity.User;

import java.sql.*;

/**
 * UserDAO: 순수 JDBC로 user 테이블 CRUD를 수행하는 클래스
 *
 * - DataSource를 생성자 주입 받아 사용
 * - 모든 메서드는 SQLException을 호출자(서비스)로 던집니다.
 */
public class UserDAO {

    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * username(로그인 ID) 중복 체크
     *
     * @param username 로그인 ID
     * @return 있으면 true, 없으면 false
     * @throws SQLException
     */
    public boolean existsByUserId(String username) throws SQLException {
        // findUserIdByUsername이 null이 아니면 “존재”로 간주
        return findUserIdByUsername(username) != null;
    }

    /**
     * username으로부터 user_id를 조회하여 반환합니다.
     * 존재하지 않으면 null을 반환합니다.
     */
    public Long findUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM `user` WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                } else {
                    return null;
                }
            }
        }
    }
    /**
     * 새 User 레코드 INSERT
     *
     * @param user User 엔티티 (id는 null이어야 함)
     * @return 생성된 PK(id) 값, 실패 시 null
     * @throws SQLException
     */
    public Long registerUser(User user) throws SQLException {
        System.out.println(">>> UserDAO.insert() 호출됨: " + user.getUsername() + "/" + user.getName());

        String sql = "INSERT INTO user(name, username, password, status) VALUES (?, ?, ?, ?)";
        Connection conn = dataSource.getConnection();
        System.out.println(">>> DB 커넥션 획득 성공: " + conn.getMetaData().getURL());
        try (PreparedStatement pstmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setBoolean(4, user.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        }
    }

    /**
     * ysername(로그인 ID) 로 User 조회
     *
     * @param username 로그인 ID
     * @return 조회된 User 엔티티, 없으면 null
     * @throws SQLException
     */
    public User SignInUser(String username) throws SQLException {
        String sql = "SELECT id, name, username, password, status FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setStatus(rs.getBoolean("status"));
                return user;
            }
        }
    }

    // --- 새로 추가된 status 업데이트 메서드 ---
    /**
     * id 기준으로 status(true/false)만 바꿔줌 (로그인/로그아웃 처리용)
     *
     * @param id     User PK
     * @param status true: 로그인, false: 로그아웃
     * @return 업데이트된 행 개수
     * @throws SQLException
     */
    public int updateStatusById(Long id, boolean status) throws SQLException {
        String sql = "UPDATE user SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, status);
            pstmt.setLong(2, id);
            return pstmt.executeUpdate();
        }
    }

}
