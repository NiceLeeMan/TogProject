package org.example.memo.dto;

import java.time.LocalDate;

/**
 * 메모 생성/수정 요청 DTO
 * 클라이언트에서 JSON으로 이 형태를 보내면,
 * 서버 쪽에서 username → user_id로 변환하여 DB에 저장한다.
 */
public class PostMemoReq {
    private String ownerUsername;   // A의 username
    private String friendUsername;  // B의 username
    private LocalDate createdDate;  // 메모 “날짜” (예: 2025-12-15)
    private String content;         // 메모 내용

    public PostMemoReq() {}

    public String getOwnerUsername() {
        return ownerUsername;
    }
    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getFriendUsername() {
        return friendUsername;
    }
    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}