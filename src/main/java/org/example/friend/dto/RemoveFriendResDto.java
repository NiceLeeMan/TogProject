package org.example.friend.dto;

import java.util.List;

import org.example.friend.info.FriendInfo;

public class RemoveFriendResDto {

	private String message;
	private List<FriendInfo> friendsList;
	
	public RemoveFriendResDto() {}
	
	public String getMessage() {
		return message;
	}
	
	public List<FriendInfo> getFriendsList(){
		return friendsList;
	}
}
