package org.example.message.dto;


import org.example.message.entity.Message;

import java.time.LocalDateTime;

/**
 * 서버가 WebSocket을 통해 브로드캐스트할 “메시지 저장 응답” DTO
 *
 * JSON 예시:
 * {
 *   "messageId": 1234,
 *   "chatRoomId": 42,
 *   "senderUsername": "alice",
 *   "content": "안녕!",
 *   "createdAt": "2025-06-04T21:15:03"
 * }
 */

public class SendMessageRes {
    private Long msgId;         // DB에 저장된 메시지의 고유 ID
    private Long chatRoomId;        // 같은 채팅방 ID
    private Long senderId;  // 보낸 사람(username)
    private String contents;         // 메시지 본문
    private LocalDateTime createdAt; // DB에서 저장된 시각

    public SendMessageRes() {}

    public SendMessageRes(Message m) {
        this.msgId = m.getMsgId();
        this.chatRoomId     = m.getRoomId();
        this.senderId = m.getSenderId();
        this.contents = m.getContents();
        this.createdAt      = m.getCreatedAt();
    }

    public Long getMsgId() {
        return msgId;
    }
    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
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
}
