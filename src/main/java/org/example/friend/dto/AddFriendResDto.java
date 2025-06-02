package org.example.friend.dto;

import java.util.List;

/**
 * 친구 추가 응답 DTO
 *
 * 요청 예시:
 * {
 *   "userId": "hong123",
 *   "friendUsername": "kim456"
 * }
 *
 * 응답 예시:
 * {
 *   "statusCode": "OK",
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
 *     },
 *     {
 *       "userId": 999,
 *       "username": "park999",
 *       "name": "박민수",
 *       "profileImgUrl": "https://..."
 *     }
 *   ]
 * }
 */
public class AddFriendResDto {
    /** "OK", "ERROR_ALREADY_FRIEND", "ERROR_NOT_FOUND", "ERROR_INTERNAL" 등 */
    private String statusCode;
    /** 친구 추가 후, 현재 친구 목록 */
    private List<FriendInfo> friendsList;

    public AddFriendResDto() {
    }

    public AddFriendResDto(String statusCode, List<FriendInfo> friendsList) {
        this.statusCode  = statusCode;
        this.friendsList = friendsList;
    }

    public String getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public List<FriendInfo> getFriendsList() {
        return friendsList;
    }
    public void setFriendsList(List<FriendInfo> friendsList) {
        this.friendsList = friendsList;
    }
}
