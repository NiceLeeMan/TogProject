package org.example.message.dto;


import java.time.LocalDateTime;

/**
 * 클라이언트가 WebSocket으로 전송하는 “메시지 저장 요청” DTO
 *
 * JSON 예시:
 * {
 *   "senderUsername": "alice",
 *   "chatRoomId": 42,
 *   "content": "안녕!",
 *   "sentAt": "2025-06-04T21:15:00"    // 선택 필드: 클라이언트 타임스탬프로도 가능
 * }
 */

public class SendMessageReq {
    private String senderUsername;  // 메시지 보낸 사람(username)
    private Long chatRoomId;        // 메시지를 보낼 채팅방 ID
    private String content;         // 메시지 본문
    private LocalDateTime sentAt;   // (선택) 클라이언트에서 찍어 보낼 수 있는 전송 시각

    public SendMessageReq() {}

    public String getSenderUsername() {
        return senderUsername;
    }
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
