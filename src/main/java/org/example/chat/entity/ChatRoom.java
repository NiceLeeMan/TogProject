package org.example.chat.entity;




import java.time.LocalDateTime;
import java.util.Objects;

/**
 * chat_room 테이블 매핑 엔티티
 *
 * - roomId   : PK (AUTO_INCREMENT)
 * - roomName : 채팅방 이름
 */
public class ChatRoom {
    private Long roomId;
    private String roomName;
    private LocalDateTime createTime;

    // 기본 생성자
    public ChatRoom() { }

    // 전체 필드를 받는 생성자
    public ChatRoom(Long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    /* ===== Getter / Setter ===== */
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoom)) return false;
        ChatRoom that = (ChatRoom) o;
        return Objects.equals(roomId, that.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }
}
