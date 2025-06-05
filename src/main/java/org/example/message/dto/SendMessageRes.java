package org.example.message.dto;


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
    private Long messageId;         // DB에 저장된 메시지의 고유 ID
    private Long chatRoomId;        // 같은 채팅방 ID
    private String senderUsername;  // 보낸 사람(username)
    private String content;         // 메시지 본문
    private LocalDateTime createdAt; // DB에서 저장된 시각

    public SendMessageRes() {}

    public Long getMessageId() {
        return messageId;
    }
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
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
}
