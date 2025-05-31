package org.example.chat.entity;


import org.example.user.entity.User;

import java.util.Objects;

/**
 * chat_room_member 테이블 엔티티
 *
 * CREATE TABLE `chat_room_member` (
 *   `id`       INT NOT NULL AUTO_INCREMENT,
 *   `room_id`  INT NOT NULL,
 *   `user_id`  INT NOT NULL,
 *   PRIMARY KEY (`id`),
 *   FOREIGN KEY (`room_id`) REFERENCES `chat_room`(`room_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 *   FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
 * );
 *
 * ‼ DAO/Service에서 chatRoom, user 연관 필드를 직접 조회하여 세팅해 주어야 함.
 */
public class ChatRoomMember {
    private Long id;        // PK
    private Long roomId;    // chat_room.room_id 참조
    private Long userId;    // user.id 참조

    /** 관계 필드 **/
    private ChatRoom chatRoom;  // roomId가 가리키는 ChatRoom 객체
    private User user;          // userId가 가리키는 User 객체

    public ChatRoomMember() { }

    public ChatRoomMember(Long id, Long roomId, Long userId) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
    }

    /***** getter / setter *****/
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ChatRoomMember{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", userId=" + userId +
                ", chatRoom=" + (chatRoom != null ? chatRoom.getRoomName() : "null") +
                ", user=" + (user != null ? user.getUserId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomMember)) return false;
        ChatRoomMember that = (ChatRoomMember) o;
        return id == that.id &&
                roomId == that.roomId &&
                userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomId, userId);
    }
}
