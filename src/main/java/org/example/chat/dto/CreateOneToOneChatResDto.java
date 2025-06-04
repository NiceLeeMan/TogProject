package org.example.chat.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CreateOneToOneChatResDto {
    // 생성된 채팅방 PK (chat_room.room_id)
    private Long chatRoomId;
    private List<MemberInfo> members;
    private LocalDateTime createdAt;

    public CreateOneToOneChatResDto() { }

    public CreateOneToOneChatResDto(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public List<MemberInfo> getMembers() {
        return members;
    }
    public void setMembers(List<MemberInfo> members) {
        this.members = members;
    }
}
