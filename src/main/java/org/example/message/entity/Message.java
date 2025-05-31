package org.example.message.entity;


import org.example.chat.entity.ChatRoom;
import org.example.user.entity.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * message 테이블 엔티티
 *
 * CREATE TABLE `message` (
 *   `msg_id`     BIGINT NOT NULL AUTO_INCREMENT,
 *   `room_id`    INT    NOT NULL,
 *   `sender_id`  INT    NOT NULL,
 *   `contents`   TEXT   NOT NULL,
 *   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *   PRIMARY KEY (`msg_id`),
 *   FOREIGN KEY (`room_id`)   REFERENCES `chat_room`(`room_id`) ON DELETE CASCADE ON UPDATE CASCADE,
 *   FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`)      ON DELETE SET NULL  ON UPDATE CASCADE
 * );
 *
 * ‼ DAO/Service에서 chatRoom, sender 연관 필드를 직접 조회하여 세팅해 주어야 함.
 */
public class Message {
    private Long msgId;               // PK
    private Long roomId;               // chat_room.room_id 참조
    private Long senderId;             // user.id 참조
    private String contents;          // 메시지 본문
    private LocalDateTime createdAt;  // 전송 시각

    /** 관계 필드 **/
    private ChatRoom chatRoom;  // 어떤 채팅방에서 보낸 메시지인지
    private User sender;        // 누가 보낸 메시지인지

    public Message() { }

    public Message(long msgId, Long roomId, Long senderId, String contents, LocalDateTime createdAt) {
        this.msgId = msgId;
        this.roomId = roomId;
        this.senderId = senderId;
        this.contents = contents;
        this.createdAt = createdAt;
    }

    /***** getter / setter *****/
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

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId=" + msgId +
                ", roomId=" + roomId +
                ", senderId=" + senderId +
                ", contents='" + contents + '\'' +
                ", createdAt=" + createdAt +
                ", chatRoom=" + (chatRoom != null ? chatRoom.getRoomName() : "null") +
                ", sender=" + (sender != null ? sender.getUserId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return msgId == message.msgId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId);
    }
}
