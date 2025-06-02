package org.example.friend.dto;


import java.util.List;

/**
 * 친구 삭제 응답 DTO
 *
 * 요청 예시:
 * {
 *   "userId": "hong123",
 *   "friendUsername": "kim456"
 * }
 *
 * 응답 예시:
 * {
 *   "message": "삭제 성공",
 *   "friendsList": [
 *     {
 *       "userId": 789,
 *       "username": "lee789",
 *       "name": "이영희",
 *       "profileImgUrl": "https://..."
 *     }
 *   ]
 * }
 */
public class RemoveFriendResDto {
    /** "삭제 성공", "친구가 없습니다", "ERROR_INTERNAL" 등 */
    private String message;
    /** 친구 삭제 후 남아 있는 현재 친구 목록 */
    private List<FriendInfo> friendsList;

    public RemoveFriendResDto() {
    }

    public RemoveFriendResDto(String message, List<FriendInfo> friendsList) {
        this.message     = message;
        this.friendsList = friendsList;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public List<FriendInfo> getFriendsList() {
        return friendsList;
    }
    public void setFriendsList(List<FriendInfo> friendsList) {
        this.friendsList = friendsList;
    }
}
