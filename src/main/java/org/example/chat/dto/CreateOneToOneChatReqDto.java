package org.example.chat.dto;


public class CreateOneToOneChatReqDto {
    // 요청하는 사용자(본인) ID
    private String username;
    // 상대 친구(대상) ID
    private String friendUsername;

    public CreateOneToOneChatReqDto() { }

    public CreateOneToOneChatReqDto(String username, String friendUsername) {
        this.username = username;
        this.friendUsername = friendUsername;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }
}
