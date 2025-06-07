package org.example.chat.dao;

import org.example.chat.dto.MemberInfo;
import org.example.message.dto.MessageInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private final DataSource dataSource;

    public ChatDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 헬퍼: username → user.id 조회
     */
    public Long getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new SQLException("해당 username을 가진 사용자가 없습니다: " + username);
            }
        }
    }
    /**
     * username으로 조회해서 해당 사용자의 name을 반환합니다.
     *
     * @param username 조회할 사용자의 username
     * @return 해당 사용자의 name
     * @throws SQLException 사용자가 없거나 DB 오류가 발생한 경우
     */
    public String getNameByUsername(String username) throws SQLException {
        String sql = "SELECT name FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
                throw new SQLException("해당 username을 가진 사용자가 없습니다: " + username);
            }
        }
    }

    /**
     * 헬퍼: room_id → room_type 조회 (ONE_TO_ONE or GROUP)
     */
    public String getRoomType(Long chatRoomId) throws SQLException {
        String sql = "SELECT room_type FROM chat_room WHERE room_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("room_type");
                }
                return null;
            }
        }
    }

    /**
     * 1:1 채팅방 생성하기
     *
     * @param creatorUsername 방을 생성하는 사용자(username)
     * @param friendUsername  상대 친구(username)
     * @return 생성된 chat_room.room_id
     * @throws SQLException
     */
    public Long insertOneToOneRoom(String creatorUsername, String friendUsername) throws SQLException {
        Long creatorId = getUserIdByUsername(creatorUsername);
        Long friendId  = getUserIdByUsername(friendUsername);

        String creatorName = getNameByUsername(creatorUsername);
        String friendName  = getNameByUsername(friendUsername);


        // 3) 방 이름을 실제 이름(name)으로 구성
        String roomName = creatorName + "&" + friendName +" 채팅방";

        // 1) chat_room에 INSERT
        String insertRoomSql = ""
                + "INSERT INTO chat_room (roomname, room_type) "
                + "VALUES (?, 'ONE_TO_ONE')";
        Long newRoomId;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertRoomSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, roomName);
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("1:1 채팅방 생성 실패: 영향 받은 행이 없습니다.");
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    newRoomId = rs.getLong(1);
                } else {
                    throw new SQLException("1:1 채팅방 room_id를 가져오지 못했습니다.");
                }
            }
        }

        // 2) chat_room_member에 creator, friend INSERT (joined_at은 최초 삽입 시 한 번만 기록)
        String insertMemberSql = ""
                + "INSERT INTO chat_room_member (room_id, user_id, joined_at) "
                + "VALUES (?, ?, NOW()) "
                + "ON DUPLICATE KEY UPDATE room_id = room_id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertMemberSql)) {

            pstmt.setLong(1, newRoomId);
            pstmt.setLong(2, creatorId);
            pstmt.executeUpdate();

            pstmt.setLong(1, newRoomId);
            pstmt.setLong(2, friendId);
            pstmt.executeUpdate();
        }

        return newRoomId;
    }

    /**
     * 그룹 채팅방 생성하기
     *
     * @param creatorUsername   방을 생성하는 사용자(username)
     * @param chatRoomName      생성할 방 이름
     * @param memberUsernames   초대할 친구(username) 리스트
     * @return 생성된 chat_room.room_id
     * @throws SQLException
     */
    public Long insertGroupRoom(String creatorUsername, String chatRoomName, List<String> memberUsernames) throws SQLException {
        Long creatorId = getUserIdByUsername(creatorUsername);

        // 1) chat_room에 INSERT
        String insertRoomSql = ""
                + "INSERT INTO chat_room (roomname, room_type) "
                + "VALUES (?, 'GROUP')";
        Long newRoomId;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertRoomSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, chatRoomName);
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("그룹 채팅방 생성 실패: 영향 받은 행이 없습니다.");
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    newRoomId = rs.getLong(1);
                } else {
                    throw new SQLException("그룹 채팅방 room_id를 가져오지 못했습니다.");
                }
            }
        }

        // 2) chat_room_member에 creator 먼저 INSERT (joined_at은 최초 삽입 시 한 번만 기록)
        String insertMemberSql = ""
                + "INSERT INTO chat_room_member (room_id, user_id, joined_at) "
                + "VALUES (?, ?, NOW()) "
                + "ON DUPLICATE KEY UPDATE room_id = room_id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertMemberSql)) {

            // 2-1) 생성자 INSERT
            pstmt.setLong(1, newRoomId);
            pstmt.setLong(2, creatorId);
            pstmt.executeUpdate();

            // 2-2) 다른 멤버들 INSERT
            for (String memberUsername : memberUsernames) {
                Long memberId = getUserIdByUsername(memberUsername);
                pstmt.setLong(1, newRoomId);
                pstmt.setLong(2, memberId);
                pstmt.executeUpdate();
            }
        }

        return newRoomId;
    }

    /**
     * 채팅방 이름 조회
     *
     * @param roomId 조회할 방의 room_id
     * @return roomname
     * @throws SQLException
     */
    public String selectRoomName(Long roomId) throws SQLException {
        String sql = "SELECT roomname FROM chat_room WHERE room_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("roomname");
                }
                throw new SQLException("해당 room_id를 찾을 수 없습니다: " + roomId);
            }
        }
    }

    /**
     * 채팅방 멤버 조회 (joinedAt 포함)
     *
     * @param chatRoomId 조회할 방의 room_id
     * @return MemberInfo 리스트
     * @throws SQLException
     */
    public List<MemberInfo> selectActiveMembers(Long chatRoomId) throws SQLException {
        String sql =
                "SELECT u.id               AS user_id, "
                        + "       u.username         AS username, "
                        + "       u.name             AS name, "
                        + "       u.profile_img_url  AS profile_img_url, "
                        + "       crm.joined_at      AS joined_at "
                        + "  FROM chat_room_member crm "
                        + "  JOIN user u ON crm.user_id = u.id "
                        + " WHERE crm.room_id = ? AND crm.left_at IS NULL "
                        + " ORDER BY crm.joined_at ASC";

        List<MemberInfo> members = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MemberInfo mi = new MemberInfo();
                    mi.setUserId(rs.getLong("user_id"));
                    mi.setUsername(rs.getString("username"));
                    mi.setName(rs.getString("name"));
                    mi.setProfileImgUrl(rs.getString("profile_img_url"));
                    mi.setJoinedAt(
                            rs.getTimestamp("joined_at")
                                    .toLocalDateTime()
                    );
                    members.add(mi);
                }
            }
        }
        return members;
    }
    /**
     * 채팅내역 읽어오기 (내 입장 시점 이후)
     *
     * @param chatRoomId 조회할 방의 room_id
     * @param username   사용자(username)
     * @return MessageInfo 리스트
     * @throws SQLException
     */
    public List<MessageInfo> selectChatHistorySinceJoin(Long chatRoomId, String username) throws SQLException {
        Long userId = getUserIdByUsername(username);

        // 1) joined_at 조회
        String joinedAtSql = "SELECT joined_at FROM chat_room_member WHERE room_id = ? AND user_id = ?";
        LocalDateTime joinedAt;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(joinedAtSql)) {

            pstmt.setLong(1, chatRoomId);
            pstmt.setLong(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("joined_at");
                    joinedAt = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                } else {
                    // 참여 기록이 없으면 빈 리스트
                    return new ArrayList<>();
                }
            }
        }

        // 2) 메시지 조회
        String sql =
                "SELECT m.msg_id           AS msg_id, " +
                        "       m.sender_id        AS sender_id, " +
                        "       u.username         AS sender_username, " +
                        "       m.contents         AS contents, " +
                        "       m.created_at       AS created_at " +
                        "  FROM message m " +
                        "  JOIN user u ON m.sender_id = u.id " +
                        " WHERE m.room_id = ? " +
                        "   AND m.created_at >= ? " +
                        " ORDER BY m.created_at ASC";

        List<MessageInfo> messages = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatRoomId);
            pstmt.setTimestamp(2, Timestamp.valueOf(joinedAt));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long    msgId          = rs.getLong("msg_id");
                    Long    senderId       = rs.getLong("sender_id");
                    String  senderUsername = rs.getString("sender_username");
                    String  contents       = rs.getString("contents");
                    Timestamp tsCreated    = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = tsCreated.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    messages.add(new MessageInfo(
                            msgId,
                            senderId,
                            senderUsername,
                            contents,
                            createdAt
                    ));
                }
            }
        }
        return messages;
    }

    /**
     * 1:1 채팅방: 해당 사용자가 활성 멤버인지 확인
     *
     * @param chatRoomId 방 ID
     * @param userId     사용자 ID
     * @return 활성 멤버이면 true, 아니면 false
     * @throws SQLException
     */
    public boolean isActiveMember(Long chatRoomId, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt "
                + "  FROM chat_room_member "
                + " WHERE room_id = ? AND user_id = ? AND left_at IS NULL";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatRoomId);
            pstmt.setLong(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    /**
     * 1:1 채팅방: 사용자가 soft‐deleted(나간 상태)였으면 복구하거나,
     * 아예 없으면 새로 INSERT (rejoin)
     *
     * @param chatRoomId 방 ID
     * @param userId     사용자 ID
     * @throws SQLException
     */
    public void reviveMemberIfNeeded(Long chatRoomId, Long userId) throws SQLException {
        // 1) 이미 soft‐deleted 된 row가 있는지 확인
        String checkSql = "SELECT COUNT(*) AS cnt "
                + "  FROM chat_room_member "
                + " WHERE room_id = ? AND user_id = ? AND left_at IS NOT NULL";

        boolean existedSoftDeleted = false;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setLong(1, chatRoomId);
            pstmt.setLong(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    existedSoftDeleted = rs.getInt("cnt") > 0;
                }
            }
        }

        if (existedSoftDeleted) {
            // 2-A) soft‐deleted 된 record → left_at = NULL, joined_at 업데이트
            String updateSql = ""
                    + "UPDATE chat_room_member "
                    + "   SET left_at = NULL, joined_at = NOW() "
                    + " WHERE room_id = ? AND user_id = ? AND left_at IS NOT NULL";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setLong(1, chatRoomId);
                pstmt.setLong(2, userId);
                pstmt.executeUpdate();
            }
        } else {
            // 2-B) row가 아예 없는 경우 → 신규 INSERT
            String insertSql = ""
                    + "INSERT INTO chat_room_member (room_id, user_id, joined_at) "
                    + "VALUES (?, ?, NOW())";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setLong(1, chatRoomId);
                pstmt.setLong(2, userId);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * 메시지 저장 (INSERT)
     *
     * @param chatRoomId 방 ID
     * @param senderId   보낸 사람 ID
     * @param contents   메시지 내용
     * @throws SQLException
     */
    public void insertMessage(Long chatRoomId, Long senderId, String contents) throws SQLException {
        String sql = ""
                + "INSERT INTO message (room_id, sender_id, contents, created_at) "
                + "VALUES (?, ?, ?, NOW())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatRoomId);
            pstmt.setLong(2, senderId);
            pstmt.setString(3, contents);
            pstmt.executeUpdate();
        }
    }

    /**
     * 사용자를 채팅방에서 나가게 처리 (ONE_TO_ONE: soft‐delete / GROUP: delete row)
     *
     * @param chatRoomId  방 ID
     * @param userId      사용자 ID
     * @param isOneToOne  일대일 채팅방인지 여부
     * @throws SQLException
     */
    public void markMemberLeft(Long chatRoomId, Long userId, boolean isOneToOne) throws SQLException {
        if (isOneToOne) {
            // 일대일 방: soft‐delete (left_at = NOW())
            String sql = ""
                    + "UPDATE chat_room_member "
                    + "   SET left_at = NOW() "
                    + " WHERE room_id = ? AND user_id = ? AND left_at IS NULL";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, chatRoomId);
                pstmt.setLong(2, userId);
                pstmt.executeUpdate();
            }
        } else {
            // 그룹 방: 행을 완전 삭제
            String sql = "DELETE FROM chat_room_member WHERE room_id = ? AND user_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, chatRoomId);
                pstmt.setLong(2, userId);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * 특정 방에 활성 멤버가 하나라도 남아 있는지 확인
     *
     * @param chatRoomId 방 ID
     * @return 하나라도 남아 있으면 true, 아니면 false
     * @throws SQLException
     */
    public boolean hasActiveMembers(Long chatRoomId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt "
                + "  FROM chat_room_member "
                + " WHERE room_id = ? AND left_at IS NULL";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    /**
     * 마지막 멤버가 나갔을 때 방(
     * chat_room_member, message, chat_room)을 물리 삭제
     *
     * @param chatRoomId 방 ID
     * @throws SQLException
     */
    public void deleteEntireChatRoom(Long chatRoomId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1) chat_room_member 모든 행 삭제
                String delMemberSql = "DELETE FROM chat_room_member WHERE room_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(delMemberSql)) {
                    pstmt.setLong(1, chatRoomId);
                    pstmt.executeUpdate();
                }

                // 2) message 모든 행 삭제
                String delMessageSql = "DELETE FROM message WHERE room_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(delMessageSql)) {
                    pstmt.setLong(1, chatRoomId);
                    pstmt.executeUpdate();
                }

                // 3) chat_room 행 삭제
                String delRoomSql = "DELETE FROM chat_room WHERE room_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(delRoomSql)) {
                    pstmt.setLong(1, chatRoomId);
                    pstmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
