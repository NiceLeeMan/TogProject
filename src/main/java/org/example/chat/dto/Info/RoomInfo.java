package org.example.chat.dto.Info;


// 3) ChatRoomInfo: DB의 chat_rooms + chat_room_member 정보를 합친 DTO
public class RoomInfo {
    private Long roomId;
    private String roomName;
    private String createdAt;

    public RoomInfo() {}

    public RoomInfo(Long roomId, String roomName, String createdAt) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.createdAt = createdAt;
    }

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}