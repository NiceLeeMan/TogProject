package org.example.chat.dto;

public class JoinChatReqDto {

    // 참여하려는 채팅방의 PK (chat_room.room_id)
    private Long chatRoomId;
    // 참여하는 사용자 본인의 PK (user.id)
    private String username;

    public JoinChatReqDto() { }

    public JoinChatReqDto(Long chatRoomId, String username) {
        this.chatRoomId = chatRoomId;
        this.username = username;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
