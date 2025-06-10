package org.example.chat.dto;

import org.example.chat.dto.Info.MemberInfo;

import java.util.List;

/**
 * 그룹 채팅방 생성 요청 DTO
 * • username     : 방을 생성하는 사용자(username)
 * • chatRoomName : 생성할 채팅방 이름
 * • members      : 채팅방에 추가할 친구 목록 (FriendInfo DTO 리스트)
 */
public class CreateGroupChatReqDto {
    private String username;
    private String chatRoomName;
    private List<MemberInfo> members;

    public CreateGroupChatReqDto() { }

    public CreateGroupChatReqDto(String username, String chatRoomName, List<MemberInfo> members) {
        this.username = username;
        this.chatRoomName = chatRoomName;
        this.members = members;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
