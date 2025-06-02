package org.example.friend.dto;

import java.util.List;


/**
 * 친구 목록 조회 응답 DTO
 *
 * 요청 예시: { "userId": "hong123" } → 응답 예시:
 * {
 *   "friendsList": [
 *     {
 *       "userId": 456,
 *       "username": "kim456",
 *       "name": "김철수",
 *       "profileImgUrl": "https://..."
 *     },
 *     {
 *       "userId": 789,
 *       "username": "lee789",
 *       "name": "이영희",
 *       "profileImgUrl": "https://..."
 *     }
 *   ]
 * }
 */
public class GetFriendsListResDto {
    private List<FriendInfo> friendsList;

    public GetFriendsListResDto() {
    }

    public GetFriendsListResDto(List<FriendInfo> friendsList) {
        this.friendsList = friendsList;
    }

    public List<FriendInfo> getFriendsList() {
        return friendsList;
    }
    public void setFriendsList(List<FriendInfo> friendsList) {
        this.friendsList = friendsList;
    }
}
