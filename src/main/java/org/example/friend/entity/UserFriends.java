package org.example.friend.entity;



import java.util.Objects;

// UserFriends.java


/**
 * user_friends 테이블 매핑 엔티티
 *
 * - userId   : “나”의 User.id (FK)
 * - friendId : “내가 추가한 친구”의 User.id (FK)
 *
 * PK는 (userId, friendId) 복합 키로 가정
 */
public class UserFriends {

    private Long userId;
    private Long friendId;

    // 기본 생성자
    public UserFriends() { }

    // 전체 필드를 받는 생성자
    public UserFriends(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    /* ===== Getter / Setter ===== */
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    @Override
    public String toString() {
        return "UserFriends{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFriends)) return false;
        UserFriends that = (UserFriends) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(friendId, that.friendId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, friendId);
    }
}
