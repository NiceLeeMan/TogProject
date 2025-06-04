package org.example.chat.dto;


import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방 나가기 응답 DTO
 * • chatRoomId : 나간 채팅방의 PK (chat_room.room_id)
 * • username   : 나간 사용자 본인의 username
 * • leftAt     : 나간 시각
 * • members    : 나간 후 해당 채팅방에 남아 있는 멤버 목록 (MemberInfo DTO 리스트)
 */
public class OutChatRoomResDto {
    private Long chatRoomId;
    private String username;
    private LocalDateTime leftAt;
    private List<MemberInfo> members;

    public OutChatRoomResDto() { }

    public OutChatRoomResDto(Long chatRoomId, String username, LocalDateTime leftAt, List<MemberInfo> members) {
        this.chatRoomId = chatRoomId;
        this.username = username;
        this.leftAt = leftAt;
        this.members = members;
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

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public List<MemberInfo> getMembers() {
        return members;
    }

    public void setMembers(List<MemberInfo> members) {
        this.members = members;
    }
}
