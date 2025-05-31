package org.example.friend.service;

import org.example.friend.dao.FriendDao;
import org.example.friend.dto.FriendListResDto;

public class FriendService {

	private FriendDao friendDao;
	
	public FriendService() {}
	
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
