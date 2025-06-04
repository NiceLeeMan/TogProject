package org.example.memo.dto;



import java.time.LocalDate;

/**
 * 특정 날짜의 메모 조회 요청 DTO
 * GET 또는 POST 방식으로 ownerUsername, friendUsername, createdDate 중 하나만 JSON/body 혹은 URL 파라미터로 전달해도 됩니다.
 */
public class GetMemoReq {
    private String ownerUsername;   // A의 username
    private String friendUsername;  // B의 username
    private LocalDate createdDate;  // 조회할 날짜

    public GetMemoReq() {}

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
}
