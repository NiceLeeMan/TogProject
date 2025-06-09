// MessageDAO.java
package org.example.message.dao;

import org.example.message.dto.MessageInfo;
import org.example.message.entity.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class MessageDAO {
    private final DataSource ds;

    public MessageDAO(DataSource ds) {
        this.ds = ds;
    }
    /**
     * 메시지 저장, 저장된 엔티티 반환 (id, createdAt 포함)
     */
    public Message save(Message msg) throws SQLException {
        String sql = "INSERT INTO message (room_id, sender_id, contents, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, msg.getRoomId());
            ps.setLong(2, msg.getSenderId());
            ps.setString(3, msg.getContents());
            ps.setTimestamp(4, Timestamp.valueOf(msg.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    msg.setMsgId(rs.getLong(1));
                }
            }
        }
        return msg;
    }


    /**
     * 특정 방의 가입 이후 메시지 조회 (사용자별)
     */
    public List<MessageInfo> selectChatHistorySinceJoin(Long roomId, String username) throws SQLException {
        String sql =
                "SELECT " +
                        "  m.msg_id        AS msgId, " +
                        "  m.room_id       AS chatRoomId, " +
                        "  m.sender_id     AS senderId, " +
                        "  u2.username     AS senderUsername, " +
                        "  m.contents      AS contents, " +
                        "  m.created_at    AS createdAt " +
                        "FROM message m " +
                        // 1) 아직 나가지 않은 멤버 정보
                        "JOIN chat_room_member cm " +
                        "  ON cm.room_id    = m.room_id " +
                        " AND cm.left_at    IS NULL " +
                        // 2) username 으로 현재 사용자의 user_id 가져오기
                        //    (JOIN user 대신 필요하면 `\"user\"` 로 감싸세요)
                        "JOIN user u1 " +
                        "  ON u1.id         = cm.user_id " +
                        " AND u1.username   = ? " +
                        // 3) 메시지 보낸 사람 조회
                        "LEFT JOIN user u2 " +
                        "  ON u2.id         = m.sender_id " +
                        // 4) 방 ID 필터 및 입장 시점 이후 메시지만
                        "WHERE m.room_id       = ? " +
                        "  AND m.created_at   >= cm.joined_at " +
                        "ORDER BY m.created_at ASC";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setLong(2, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                List<MessageInfo> list = new ArrayList<>();
                while (rs.next()) {
                    MessageInfo info = new MessageInfo();
                    info.setMsgId(rs.getLong("msgId"));
                    info.setRoomId(rs.getLong("chatRoomId"));
                    info.setSenderId(rs.getLong("senderId"));
                    info.setSenderUsername(rs.getString("senderUsername"));
                    info.setContents(rs.getString("contents"));
                    info.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                    list.add(info);
                }
                return list;
            }
        }
    }

    // 사용자명으로 ID 조회 (user 테이블 사용)
    private Long getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM user WHERE username = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }
}
