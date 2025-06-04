package org.example.chat.dto;

import org.example.message.dto.MessageInfo;

import java.time.LocalDateTime;
import java.util.List;

public class JoinChatResDto {

    private Long chatRoomId;
    private String chatRoomName;
    private List<MemberInfo> members;
    // 해당 채팅방의 과거 채팅 내역 (MessageInfoDto 리스트)
    private List<MessageInfo> messages;
    private LocalDateTime joinAt;


    public JoinChatResDto() { }

    public JoinChatResDto(
            Long chatRoomId,
            String chatRoomName,
            List<MemberInfo> members,
            List<MessageInfo> messages
    ) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.members = members;
        this.messages = messages;
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

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageInfo> messages) {
        this.messages = messages;
    }

    public LocalDateTime getJoinAt() {
        return joinAt;
    }
    public void setJoinAt(LocalDateTime joinAt) {
        this.joinAt = joinAt;
    }
}
