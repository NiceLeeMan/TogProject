package org.example.chat.entity;



import org.example.message.entity.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * chat_room 테이블 엔티티
 *
 * CREATE TABLE `chat_room` (
 *   `room_id`  INT          NOT NULL AUTO_INCREMENT,
 *   `roomname` VARCHAR(100) NOT NULL,
 *   PRIMARY KEY (`room_id`)
 * );
 *
 * ‼ DAO/Service에서 members, messages 필드를 직접 조회하여 세팅해 주어야 함.
 */
public class ChatRoom {
    private Long roomId;          // PK
    private String roomName;     // 채팅방 이름

    /** 관계 필드 **/
    // 1) 이 채팅방에 속한 모든 멤버 목록
    private List<ChatRoomMember> members = new ArrayList<>();

    // 2) 이 채팅방에서 주고받은 모든 메시지 목록
    private List<Message> messages = new ArrayList<>();

    public ChatRoom() { }

    public ChatRoom(Long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    /***** getter / setter *****/
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<ChatRoomMember> getMembers() {
        return members;
    }

    public void setMembers(List<ChatRoomMember> members) {
        this.members = members;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", members=" + members +
                ", messages=" + messages +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoom)) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return roomId == chatRoom.roomId &&
                Objects.equals(roomName, chatRoom.roomName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, roomName);
    }
}
