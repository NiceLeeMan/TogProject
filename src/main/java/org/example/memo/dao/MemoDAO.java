package org.example.memo.dao;


import org.example.memo.entity.Memo;

import java.sql.*;
import java.time.LocalDate;
import javax.sql.DataSource;

public class MemoDAO {
    private final DataSource dataSource;

    public MemoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 1) (ownerId, friendId, createdDate) 조합으로 메모가 있는지 조회
     */
    public Memo findByOwnerFriendDate(Long ownerId, Long friendId, LocalDate createdDate) throws SQLException {
        String sql = "SELECT memo_id, content FROM memo "
                + "WHERE owner_id = ? AND friend_id = ? AND created_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            ps.setLong(2, friendId);
            ps.setDate(3, Date.valueOf(createdDate));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Memo e = new Memo();
                    e.setMemoId(rs.getLong("memo_id"));
                    e.setOwnerId(ownerId);
                    e.setFriendId(friendId);
                    e.setCreatedAt(createdDate);
                    e.setContent(rs.getString("content"));
                    return e;
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * 2) INSERT
     */
    public Memo insertMemo(Memo entity) throws SQLException {
        String sql = "INSERT INTO memo (owner_id, friend_id, created_date, content) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entity.getOwnerId());
            ps.setLong(2, entity.getFriendId());
            ps.setDate(3, Date.valueOf(entity.getCreatedAt()));
            ps.setString(4, entity.getContent());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("메모 INSERT 실패 (affected = 0)");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setMemoId(rs.getLong(1));
                }
            }
            return entity;
        }
    }

    /**
     * 3) UPDATE (content만 교체)
     */
    public boolean updateMemoContent(Long ownerId, Long friendId, LocalDate createdDate, String newContent) throws SQLException {
        String sql = "UPDATE memo SET content = ? "
                + "WHERE owner_id = ? AND friend_id = ? AND created_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setLong(2, ownerId);
            ps.setLong(3, friendId);
            ps.setDate(4, Date.valueOf(createdDate));

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * 4) DELETE (특정 날짜 메모 삭제)
     */
    public boolean deleteMemo(Long ownerId, Long friendId, LocalDate createdDate) throws SQLException {
        String sql = "DELETE FROM memo WHERE owner_id = ? AND friend_id = ? AND created_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            ps.setLong(2, friendId);
            ps.setDate(3, Date.valueOf(createdDate));

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * 5) MySQL Upsert 예시 (INSERT 혹은 UPDATE 한 줄로 처리)
     *    → 단, UNIQUE(owner_id, friend_id, created_date) 제약이 있어야 동작.
     */
    public void upsertMemo(Memo entity) throws SQLException {
        String sql = ""
                + "INSERT INTO memo (owner_id, friend_id, created_date, content) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE content = VALUES(content)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, entity.getOwnerId());
            ps.setLong(2, entity.getFriendId());
            ps.setDate(3, Date.valueOf(entity.getCreatedAt()));
            ps.setString(4, entity.getContent());
            ps.executeUpdate();
        }
    }
}
