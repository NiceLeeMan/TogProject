package org.example.chat.dao;

import org.example.chat.dto.MemberInfo;
import org.example.chat.dto.OutChatRoomReqDto;
import org.example.chat.dto.OutChatRoomResDto;
import org.example.message.dto.MessageInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneId;



public class ChatDAO {

    private final DataSource dataSource;

    public ChatDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 헬퍼: username → user.id 조회
     */
    private Long getUserIdByUsername(String username) throws SQLException {
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
    // 기존 메서드: 방 유형 조회
    public String getRoomType(Long chatRoomId) throws SQLException {
        String sql = "SELECT room_type FROM chat_room WHERE room_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("room_type") : null;
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
    public Long createOneToOneChat(String creatorUsername, String friendUsername) throws SQLException {
        Long creatorId = getUserIdByUsername(creatorUsername);
        Long friendId  = getUserIdByUsername(friendUsername);

        String roomName = creatorUsername + "_" + friendUsername;

        // chat_room에 INSERT
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

        // chat_room_member에 creator, friend INSERT (joined_at은 최초 삽입 시 한 번만 기록)
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
     * @param creatorUsername 방을 생성하는 사용자(username)
     * @param chatRoomName    생성할 방 이름
     * @param memberUsernames 초대할 친구(username) 리스트
     * @return 생성된 chat_room.room_id
     * @throws SQLException
     */
    public Long createGroupChat(String creatorUsername, String chatRoomName, List<String> memberUsernames) throws SQLException {
        Long creatorId = getUserIdByUsername(creatorUsername);

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

        // chat_room_member에 creator 먼저 INSERT (joined_at은 최초 삽입 시 한 번만 기록)
        String insertMemberSql = ""
                + "INSERT INTO chat_room_member (room_id, user_id, joined_at) "
                + "VALUES (?, ?, NOW()) "
                + "ON DUPLICATE KEY UPDATE room_id = room_id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertMemberSql)) {

            // 1) 생성자 INSERT
            pstmt.setLong(1, newRoomId);
            pstmt.setLong(2, creatorId);
            pstmt.executeUpdate();

            // 2) 다른 멤버들 INSERT
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
     * 채팅방 참여 처리은 별도 INSERT 없이,
     * 단지 방을 보여주기 위해 이름, 멤버, 메시지 조회만 할 때 사용합니다.
     */

    /**
     * 채팅방 이름 조회
     *
     * @param roomId 조회할 방의 room_id
     * @return roomname
     * @throws SQLException
     */
    public String findRoomNameById(Long roomId) throws SQLException {
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
    public List<MemberInfo> getChatRoomMembers(Long chatRoomId) throws SQLException {
        String sql =
                "SELECT u.id               AS user_id, " +
                        "       u.username         AS username, " +
                        "       u.name             AS name, " +
                        "       u.profile_img_url  AS profile_img_url, " +
                        "       crm.joined_at      AS joined_at " +
                        "  FROM chat_room_member crm " +
                        "  JOIN user u ON crm.user_id = u.id " +
                        " WHERE crm.room_id = ? " +
                        " ORDER BY crm.joined_at ASC";

        List<MemberInfo> members = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chatRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long userId           = rs.getLong("user_id");
                    String username       = rs.getString("username");
                    String name           = rs.getString("name");
                    String profileImgUrl  = rs.getString("profile_img_url");
                    Timestamp tsJoined    = rs.getTimestamp("joined_at");
                    LocalDateTime joinedAt = tsJoined.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    members.add(new MemberInfo(
                            userId,
                            username,
                            name,
                            profileImgUrl,
                            joinedAt
                    ));
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
    public List<MessageInfo> getChatHistory(Long chatRoomId, String username) throws SQLException {
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
                    return new ArrayList<>(); // 참여 기록이 없으면 빈 리스트
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
     * 사용자를 채팅방에서 나가게 처리합니다.
     *
     *  • 일대일(ONE_TO_ONE) 방: left_at = NOW() (soft‐delete)
     *  • 그룹(GROUP) 방: chat_room_member 행을 삭제
     */
    public OutChatRoomResDto leaveChatRoom(OutChatRoomReqDto reqDto) throws SQLException {
        Long chatRoomId = reqDto.getChatRoomId();
        String username = reqDto.getUsername();

        // 1) username → user_id
        Long userId = getUserIdByUsername(username);
        if (userId == null) {
            throw new SQLException("존재하지 않는 사용자: " + username);
        }

        // 2) 방 유형 조회
        String roomType = getRoomType(chatRoomId);
        if (roomType == null) {
            throw new SQLException("존재하지 않는 채팅방 ID: " + chatRoomId);
        }

        if ("ONE_TO_ONE".equals(roomType)) {
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
        // 4) 나간 후에도 남아 있는 active 멤버 목록 조회 (엔티티→DTO 변환)
        List<MemberInfo> remainingMembers = getActiveMembers(chatRoomId);
        // 5) 응답 DTO 생성

        OutChatRoomResDto res = new OutChatRoomResDto();
        res.setChatRoomId(chatRoomId);
        res.setUsername(username);
        res.setLeftAt(LocalDateTime.now());
        res.setMembers(remainingMembers);
        return res;
    }
    public List<MemberInfo> getActiveMembers(Long chatRoomId) throws SQLException {
        String sql = ""
                + "SELECT u.user_id, u.username, u.display_name, u.profile_img_url "
                + "  FROM users u "
                + "  JOIN chat_room_member m ON u.user_id = m.user_id "
                + " WHERE m.room_id = ? AND m.left_at IS NULL";

        List<MemberInfo> list = new ArrayList<>();
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
                    list.add(mi);
                }
            }
        }
        return list;
    }

}
