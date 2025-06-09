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
        // 1) 사용자 ID 조회
        Long userId = getUserIdByUsername(username);
        if (userId == null) {
            return new ArrayList<>();
        }

        // 2) 가입 시각 조회
        LocalDateTime joinedAt;
        String joinedSql = "SELECT joined_at FROM chat_room_member WHERE room_id = ? AND user_id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(joinedSql)) {
            ps.setLong(1, roomId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    joinedAt = rs.getTimestamp("joined_at").toLocalDateTime();
                } else {
                    return new ArrayList<>();
                }
            }
        }

        // 3) 메시지 조회
        String sql = "SELECT message_id, room_id, sender_id, contents, created_at " +
                "FROM message WHERE room_id = ? AND created_at >= ? ORDER BY created_at";
        List<MessageInfo> list = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            ps.setTimestamp(2, Timestamp.valueOf(joinedAt));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MessageInfo info = new MessageInfo();
                    info.setMsgId(rs.getLong("message_id"));
                    info.setRoomId(rs.getLong("room_id"));
                    info.setSenderId(rs.getLong("sender_id"));
                    info.setContents(rs.getString("contents"));
                    info.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(info);
                }
            }
        }
        return list;
    }

    // 사용자명으로 ID 조회 (user 테이블 사용)
    private Long getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
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
