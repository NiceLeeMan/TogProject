package org.example.friend.dao;


import org.example.friend.dto.FriendInfo;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * ERD 기준:
 *   • user (id BIGINT PK, name VARCHAR(50), username VARCHAR(50), password VARCHAR(255), status TINYINT(1), profile_img_url VARCHAR(255))
 *   • user_friends (user_id BIGINT FK → user.id, friend_id BIGINT FK → user.id)
 */
public class FriendDAO {

    private final DataSource dataSource;

    public FriendDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 1. 나에게 등록된 친구를 읽어와 리스트에 담는 메서드
     *    파라미터로 받은 username으로 user.id를 조회한 뒤,
     *    user_friends 테이블에서 friend_id를 가져와
     *    user 테이블과 JOIN하여 FriendInfo DTO로 매핑한다.
     */
    public List<FriendInfo> getFriendList(String username) throws SQLException {
        // 1) username → 내 user.id 조회
        Long myUserId = getUserIdByUsername(username);

        // 2) user_friends에서 내 user_id에 해당하는 friend_id 목록 조회 → user 테이블에서 친구 정보 가져오기
        String sql =
                "SELECT u.id                 AS user_id, " +
                        "       u.username           AS username, " +
                        "       u.name               AS name, " +
                        "       u.profile_img_url    AS profile_img_url, " +
                        "       u.status             AS status " +    // status 컬럼을 boolean로 읽어서 online 여부로 사용
                        "FROM user_friends uf " +
                        "JOIN user u ON uf.friend_id = u.id " +
                        "WHERE uf.user_id = ?";

        List<FriendInfo> friends = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, myUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long   friendId      = rs.getLong("user_id");
                    String friendUsername= rs.getString("username");
                    String friendName    = rs.getString("name");
                    String profileUrl    = rs.getString("profile_img_url");
                    boolean Online       = rs.getBoolean("status");

                    FriendInfo info = new FriendInfo(
                            friendId,
                            friendName,
                            friendUsername,
                            profileUrl,
                            Online

                    );
                    friends.add(info);
                }
            }
        }

        return friends;
    }

    /**
     * 2. 친구를 추가하는 메서드 (단방향)
     *    내 username, 추가할 친구의 username을 받아서
     *    각각 user.id를 구한 뒤 user_friends 테이블에 INSERT 한다.
     */
    public void addFriend(String username, String friendUsername) throws SQLException {
        Long userId       = getUserIdByUsername(username);
        Long friendUserId = getUserIdByUsername(friendUsername);

        String sql = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, friendUserId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 3. 친구를 삭제하는 메서드 (단방향)
     *    내 username, 삭제할 친구의 username을 받아서
     *    user_friends 테이블에서 DELETE 한다.
     */
    public void removeFriend(String username, String friendUsername) throws SQLException {
        Long userId       = getUserIdByUsername(username);
        Long friendUserId = getUserIdByUsername(friendUsername);

        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, friendUserId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 헬퍼 메서드: username → user.id 조회
     * 해당 username이 존재하지 않으면 SQLException 발생
     */
    private Long getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM user WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                } else {
                    throw new SQLException("해당 username을 가진 사용자가 없습니다: " + username);
                }
            }
        }
    }
}
