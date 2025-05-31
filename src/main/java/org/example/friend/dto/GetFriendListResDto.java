package org.example.friend.dto;

import org.example.friend.info.FriendInfo;
import java.util.List;

public class GetFriendListResDto {
	
	private List<FriendInfo> friendsList;
	
	public GetFriendListResDto() {}
	
	
	public List<FriendInfo> getFriendsList(){
		return friendsList;
	}
	
}
 