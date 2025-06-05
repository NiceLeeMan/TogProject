// MessageDAO.java
package org.example.message.dao;

import org.example.message.entity.Message;

import java.sql.*;
import javax.sql.DataSource;

public class MessageDAO {
    private final DataSource dataSource;

    public MessageDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 1) 메시지 저장 (INSERT)
     *
     * INSERT INTO message (chat_room_id, sender_id, content)
     * VALUES (?, ?, ?);
     *
     * 저장 후, 생성된 message_id와 DB의 created_at 컬럼을 다시 조회해서
     * Message 엔티티에 채워 돌려줍니다.
     */
    public Message insertMessage(Message msg) throws SQLException {
        String sql = "INSERT INTO message (chat_room_id, sender_id, content) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, msg.getRoomId());
            ps.setLong(2, msg.getSenderId());
            ps.setString(3, msg.getContents());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("메시지 INSERT 실패: affected = 0");
            }

            // 생성된 message_id를 가져오기
            long generatedId;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getLong(1);
                    msg.setMsgId(generatedId);
                } else {
                    throw new SQLException("메시지 INSERT 실패: 생성된 키를 가져올 수 없음");
                }
            }

            // 방금 삽입된 행의 created_at 값을 가져오기 위해 다시 SELECT
            String selectSql = "SELECT created_at FROM message WHERE message_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(selectSql)) {
                ps2.setLong(1, msg.getMsgId());
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        Timestamp ts = rs2.getTimestamp("created_at");
                        if (ts != null) {
                            msg.setCreatedAt(ts.toLocalDateTime());
                        }
                    }
                }
            }

            return msg;
        }
    }
}
