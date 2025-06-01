package org.example.memo.entity;



import java.time.LocalDateTime;
import java.util.Objects;

// Memo.java

/**
 * memo 테이블 매핑 엔티티
 *
 * - memoId    : PK (AUTO_INCREMENT)
 * - ownerId   : 메모 작성자 User.id (FK)
 * - friendId  : 메모 대상 User.id (FK)
 * - content   : 메모 본문
 * - createdAt : 작성 시각 (LocalDateTime)
 */

public class Memo {
    private Long memoId;
    private Long ownerId;
    private Long friendId;
    private String content;
    private LocalDateTime createdAt;

    // 기본 생성자
    public Memo() { }

    // 전체 필드를 받는 생성자
    public Memo(Long memoId, Long ownerId, Long friendId, String content, LocalDateTime createdAt) {
        this.memoId = memoId;
        this.ownerId = ownerId;
        this.friendId = friendId;
        this.content = content;
        this.createdAt = createdAt;
    }

    /* ===== Getter / Setter ===== */
    public Long getMemoId() {
        return memoId;
    }

    public void setMemoId(Long memoId) {
        this.memoId = memoId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Memo{" +
                "memoId=" + memoId +
                ", ownerId=" + ownerId +
                ", friendId=" + friendId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Memo)) return false;
        Memo that = (Memo) o;
        return Objects.equals(memoId, that.memoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoId);
    }
}
