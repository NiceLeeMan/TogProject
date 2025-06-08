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

    // 메시지를 보낼 채팅방 ID
    private Long roomId;
    private Long senderId;  // 메시지 보낸 사람(username)
    private String contents;         // 메시지 본문// (선택) 클라이언트에서 찍어 보낼 수 있는 전송 시각

    public SendMessageReq() {}

    public Long getSenderId() {
        return senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRoomId() {
        return roomId;
    }
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getContents() {
        return contents;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }

}
