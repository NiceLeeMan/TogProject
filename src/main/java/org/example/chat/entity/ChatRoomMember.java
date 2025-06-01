package org.example.chat.entity;



import java.util.Objects;


/**
 * chat_room_member 테이블 매핑 엔티티
 * - id     : PK (AUTO_INCREMENT)
 * - roomId : 참조하는 채팅방 ChatRoom.roomId (FK)
 * - userId : 참조하는 사용자 User.id (FK)
 */

public class ChatRoomMember {
    private Long id;
    private Long roomId;
    private Long userId;

    // 기본 생성자
    public ChatRoomMember() { }

    // 전체 필드를 받는 생성자
    public ChatRoomMember(Long id, Long roomId, Long userId) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
    }

    /* ===== Getter / Setter ===== */
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

    @Override
    public String toString() {
        return "ChatRoomMember{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", userId=" + userId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomMember)) return false;
        ChatRoomMember that = (ChatRoomMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
