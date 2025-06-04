package org.example.memo.dto;


import java.time.LocalDate;

public class PostMemoRes {
    private String message;     // “메모 저장 성공” 등
    private Long memoId;        // DB에 최종 저장된 memo_id
    private LocalDate createdDate; // 클라이언트가 전달한 createdDate 를 그대로 반환해도 무방

    public PostMemoRes() {}

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Long getMemoId() {
        return memoId;
    }
    public void setMemoId(Long memoId) {
        this.memoId = memoId;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}
