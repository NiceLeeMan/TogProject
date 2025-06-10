package org.example.chat.dto.GetRoom;

import org.example.chat.dto.Info.RoomInfo;

import java.util.List;

public class GetRoomsRes {

//    private String username;

    private List<RoomInfo> rooms;

    public GetRoomsRes() {}

    public GetRoomsRes(List<RoomInfo> rooms) {
//        this.username = username;
        this.rooms = rooms;
    }


    public List<RoomInfo> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomInfo> rooms) {
        this.rooms = rooms;
    }
}