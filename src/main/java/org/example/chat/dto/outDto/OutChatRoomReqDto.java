package org.example.chat.dto.outDto;





/**
 * 채팅방 나가기 요청 DTO
 * • chatRoomId : 나가려는 채팅방의 PK (chat_room.room_id)
 * • username   : 나가는 사용자 본인의 username
 */
public class OutChatRoomReqDto {
    private Long chatRoomId;
    private String username;

    public OutChatRoomReqDto() { }

    public OutChatRoomReqDto(Long chatRoomId, String username) {
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

