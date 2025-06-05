package org.example.memo.dao;

import org.example.memo.entity.Memo;

import java.sql.*;
import javax.sql.DataSource;

public class MemoDAO {
    private final DataSource dataSource;

    public MemoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 1) (ownerId, friendId, createdDate) 조합으로 메모가 있는지 조회
     *    → 매개변수로 Memo 엔티티를 받아서 내부에서 필요한 필드를 꺼냅니다.
     */
    public Memo findByOwnerFriendDate(Memo memoReq) throws SQLException {
        String sql = "SELECT memo_id, content, created_date "
                + "FROM memo "
                + "WHERE owner_id = ? AND friend_id = ? AND created_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memoReq.getOwnerId());
            ps.setLong(2, memoReq.getFriendId());
            ps.setDate(3, Date.valueOf(memoReq.getCreatedAt()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Memo found = new Memo();
                    found.setMemoId(rs.getLong("memo_id"));
                    found.setOwnerId(memoReq.getOwnerId());
                    found.setFriendId(memoReq.getFriendId());
                    found.setCreatedAt(rs.getDate("created_date").toLocalDate());
                    found.setContent(rs.getString("content"));
                    return found;
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * 2) INSERT
     *    → 이미 Memo 엔티티를 매개변수로 받아 내부의 ownerId, friendId, createdDate, content를 사용.
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
     *    → 매개변수로 Memo 엔티티를 받아 content, ownerId, friendId, createdDate를 사용.
     */
    public boolean updateMemoContent(Memo memo) throws SQLException {
        String sql = "UPDATE memo SET content = ? "
                + "WHERE owner_id = ? AND friend_id = ? AND created_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memo.getContent());
            ps.setLong(2, memo.getOwnerId());
            ps.setLong(3, memo.getFriendId());
            ps.setDate(4, Date.valueOf(memo.getCreatedAt()));

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * 4) DELETE (특정 날짜 메모 삭제)
     *    → Memo 엔티티에서 ownerId, friendId, createdDate를 꺼내 사용.
     */
    public boolean deleteMemo(Memo memoReq) throws SQLException {
        String sql = "DELETE FROM memo "
                + "WHERE owner_id = ? AND friend_id = ? AND created_date = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memoReq.getOwnerId());
            ps.setLong(2, memoReq.getFriendId());
            ps.setDate(3, Date.valueOf(memoReq.getCreatedAt()));

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * 5) MySQL Upsert 예시 (INSERT 혹은 UPDATE 한 줄로 처리)
     *    → Memo 엔티티에서 ownerId, friendId, createdDate, content를 사용.
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
