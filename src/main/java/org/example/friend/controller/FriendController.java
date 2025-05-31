package org.example.friend.controller;

import org.example.friend.dto.FriendListResDto;
import org.example.friend.service.FriendService;

public class FriendController {

	private FriendService friendService;
	
	public FriendController() {}
	
	public void addFriend(String userId, String targetId) {
		
	}
	
	public void removeFriend(String userId, String targetId) {
		
	}
	
	public FriendListResDto getFriends(String userId) {
		// 임시로 해놓음
		FriendListResDto g = null;
		return g;
	} 
	
	public FriendListResDto searchFriend(String user, String keyword) {
		// 임시로 해놓음
		FriendListResDto g = null;
		return g;
	}
}
