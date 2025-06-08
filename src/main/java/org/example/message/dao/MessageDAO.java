// MessageDAO.java
package org.example.message.dao;

import org.example.message.entity.Message;

import java.sql.*;
import java.time.LocalDateTime;
import javax.sql.DataSource;

public class MessageDAO {
    private final DataSource ds;

    public MessageDAO(DataSource ds) {
        this.ds = ds;
    }

    public Message save(Message msg) throws SQLException {
        String sql = "INSERT INTO message (room_id, sender_id, contents) VALUES (?, ?, ?)";
        try (Connection conn = ds.getConnection();
             // 생성 키로 message_id와 created_at을 명시적으로 요청
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     new String[]{ "message_id", "created_at" }
             )) {

            ps.setLong(1, msg.getRoomId());
            ps.setLong(2, msg.getSenderId());
            ps.setString(3, msg.getContents());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong("message_id");
                    LocalDateTime createdAt =
                            rs.getTimestamp("created_at").toLocalDateTime();
                    return new Message(id,
                            msg.getRoomId(),
                            msg.getSenderId(),
                            msg.getContents(),
                            createdAt);
                }
            }
            throw new SQLException("Message 저장 후 키 획득 실패");
        }
    }
}
