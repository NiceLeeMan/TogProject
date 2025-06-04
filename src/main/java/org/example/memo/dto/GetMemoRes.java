package org.example.memo.dto;



import java.time.LocalDate;

public class GetMemoRes {
    private String content;        // 해당 날짜의 메모 내용 (없으면 빈 문자열 또는 null)
    private LocalDate createdDate; // 조회된 날짜 (클라이언트가 요청한 날짜)
    // (추가로 memoId가 필요하면 여기에 담을 수도 있습니다.)

    public GetMemoRes() {}

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}
