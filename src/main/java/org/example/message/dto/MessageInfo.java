package org.example.message.dto;

import java.time.LocalDateTime;

public class MessageInfo {
    // 메시지 고유 ID (message.msg_id)
    private Long msgId;

    // 메시지를 보낸 사용자 ID (message.sender_id)
    private Long senderId;

    // (선택) 메시지를 보낸 사용자의 이름이나 username
    // 여기서는 username(String)으로 예시를 들었습니다.
    private String senderUsername;

    // 메시지 내용 (message.contents)
    private String contents;

    // 메시지 생성 시각 (message.created_at)
    private LocalDateTime createdAt;

    public MessageInfo() { }

    public MessageInfo(Long msgId, Long senderId, String senderUsername, String contents, LocalDateTime createdAt) {
        this.msgId = msgId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.contents = contents;
        this.createdAt = createdAt;
    }

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
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
