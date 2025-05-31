package org.example.friend.entity;



import org.example.user.entity.User;

import java.util.Objects;


/**
 * user_friends 테이블 엔티티
 *
 * CREATE TABLE `user_friends` (
 *   `user_id`   INT NOT NULL,
 *   `friend_id` INT NOT NULL,
 *   PRIMARY KEY (`user_id`, `friend_id`),
 *   FOREIGN KEY (`user_id`)   REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
 *   FOREIGN KEY (`friend_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
 * );
 *
 * ‼ DAO/Service에서 user, friend 연관 필드를 직접 조회하여 세팅해 주어야 함.
 */

public class UserFriends {
    private Long userId;    // user.id 참조
    private Long friendId;  // user.id 참조

    /** 관계 필드 **/
    private User user;     // userId가 참조하는 User 객체
    private User friend;   // friendId가 참조하는 User 객체

    public UserFriends() { }

    public UserFriends(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    /***** getter / setter *****/
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    @Override
    public String toString() {
        return "UserFriends{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                ", user=" + (user != null ? user.getUserId() : "null") +
                ", friend=" + (friend != null ? friend.getUserId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFriends)) return false;
        UserFriends that = (UserFriends) o;
        return userId == that.userId &&
                friendId == that.friendId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, friendId);
    }
}
