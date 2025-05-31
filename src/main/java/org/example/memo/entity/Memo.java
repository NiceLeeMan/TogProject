package org.example.memo.entity;



import org.example.user.entity.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * memo 테이블 엔티티
 *
 * CREATE TABLE `memo` (
 *   `memo_id`    INT      NOT NULL AUTO_INCREMENT,
 *   `owner_id`   INT      NOT NULL,
 *   `friend_id`  INT      NOT NULL,
 *   `content`    TEXT     NOT NULL,
 *   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   PRIMARY KEY (`memo_id`),
 *   FOREIGN KEY (`owner_id`)  REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
 *   FOREIGN KEY (`friend_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
 * );
 *
 * ‼ DAO/Service에서 owner, friend 연관 필드를 직접 조회하여 세팅해 주어야 함.
 */
public class Memo {
    private Long memoId;               // PK
    private Long ownerId;              // User.id 참조
    private Long friendId;             // User.id 참조
    private String content;           // 메모 본문
    private LocalDateTime createdAt;  // 작성 시각

    /** 관계 필드 **/
    private User owner;   // 메모를 보낸 사람(객체)
    private User friend;  // 메모를 받은 사람(객체)

    public Memo() { }

    public Memo(Long memoId, Long ownerId, Long friendId, String content, LocalDateTime createdAt) {
        this.memoId = memoId;
        this.ownerId = ownerId;
        this.friendId = friendId;
        this.content = content;
        this.createdAt = createdAt;
    }

    /***** getter / setter *****/
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    @Override
    public String toString() {
        return "Memo{" +
                "memoId=" + memoId +
                ", ownerId=" + ownerId +
                ", friendId=" + friendId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", owner=" + (owner != null ? owner.getUserId() : "null") +
                ", friend=" + (friend != null ? friend.getUserId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Memo)) return false;
        Memo memo = (Memo) o;
        return memoId == memo.memoId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoId);
    }
}
