package org.example.friend.dto;

import java.util.List;
import org.example.friend.info.FriendInfo;

public class AddFriendResDto {

	private String statusCode;
	private List<FriendInfo> friendsList;
	
	public AddFriendResDto() {};
	
	public String getStatusCode() {
		return statusCode;
	}
	
	
	public List<FriendInfo> getFriendsList(){
		return friendsList;
	}
}
