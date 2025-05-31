package org.example.friend.dao;

import java.util.List;
import javax.sql.DataSource;
import org.example.user.entity.User;
import org.example.friend.dto.FriendDto;

public class FriendDao {
	
	private DataSource dataSource;
	
	
	public FriendDao() {} 
	
	public void insertFriend(String userId, User target) {
		
	}
	
	public void deleteFriend(String userId, User target) {
		
	}
	
	public List<FriendDto> findFriends(String userId){
		// 임시로 해놓음
		List<FriendDto> fd = null;
		return fd;
	}
}
