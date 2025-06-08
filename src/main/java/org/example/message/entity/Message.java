package org.example.message.entity;


import java.time.LocalDateTime;
import java.util.Objects;

// Message.java


/**
 * message 테이블 매핑 엔티티
 *
 * - msgId     : PK (AUTO_INCREMENT)
 * - roomId    : 메시지가 속한 채팅방 ChatRoom.roomId (FK)
 * - senderId  : 메시지를 보낸 사용자 User.id (FK)
 * - contents  : 메시지 본문
 * - createdAt : 전송 시각 (LocalDateTime)
 */
public class Message {
    private Long msgId;
    private Long roomId;
    private Long senderId;
    private String contents;
    private LocalDateTime createdAt;

    // 기본 생성자
    public Message() { }

    // 전체 필드를 받는 생성자
    public Message(Long msgId, Long roomId, Long senderId, String contents, LocalDateTime createdAt) {
        this.msgId = msgId;
        this.roomId = roomId;
        this.senderId = senderId;
        this.contents = contents;
        this.createdAt = createdAt;
    }

    /* ===== Getter / Setter ===== */
    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId=" + msgId +
                ", roomId=" + roomId +
                ", senderId=" + senderId +
                ", contents='" + contents + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message that = (Message) o;
        return Objects.equals(msgId, that.msgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId);
    }
}
