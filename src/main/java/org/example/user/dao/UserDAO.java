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
     * user_id(로그인 ID) 중복 체크
     *
     * @param userId 로그인 ID
     * @return 중복이면 true, 아니면 false
     * @throws SQLException
     */
    public boolean existsByUserId(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
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
    public User findByUserId(String username) throws SQLException {
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

    /**
     * id(PK) 로 User 조회
     *
     * @param id PK
     * @return 조회된 User 엔티티, 없으면 null
     * @throws SQLException
     */
    public User findById(Long id) throws SQLException {
        String sql = "SELECT id, name, username, password, status FROM user WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
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

    /**
     * User 상태(status) 업데이트 (로그인 시 true, 로그아웃 시 false)
     *
     * @param id     PK
     * @param status 변경할 상태값
     * @return 업데이트된 행 개수 (정상 시 1)
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


    /**
     * User 삭제 (관리용/테스트용)
     *
     * @param id 삭제할 사용자 PK
     * @return 삭제된 행 개수
     * @throws SQLException
     */
    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate();
        }
    }
}
