package org.example.chat.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 그룹 채팅방 생성 응답 DTO
 * • chatRoomId   : 생성된 채팅방의 PK (chat_room.room_id)
 * • chatRoomName : 채팅방 이름 (chat_room.roomname)
 * • members      : 방에 속한 멤버(MemberInfo) DTO 리스트)
 * • createdAt    : 채팅방 생성 시각
 */
public class CreateGroupChatResDto {
    private Long chatRoomId;
    private String chatRoomName;
    private List<MemberInfo> members;
    private LocalDateTime createdAt;

    public CreateGroupChatResDto() { }

    public CreateGroupChatResDto(Long chatRoomId, String chatRoomName, List<MemberInfo> members, LocalDateTime createdAt) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.members = members;
        this.createdAt = createdAt;
    }

    public Long getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public List<MemberInfo> getMembers() {
        return members;
    }

    public void setMembers(List<MemberInfo> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
