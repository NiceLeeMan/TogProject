package org.example.memo.service;



import org.example.memo.dao.MemoDAO;
import org.example.memo.dto.GetMemoReq;
import org.example.memo.dto.GetMemoRes;
import org.example.memo.dto.PostMemoReq;
import org.example.memo.dto.PostMemoRes;
import org.example.memo.entity.Memo;
import org.example.user.dao.UserDAO;
import java.sql.SQLException;
import java.time.LocalDate;

// MemoService.java


import java.sql.SQLException;

/**
 * MemoService는 DTO를 매개변수와 반환값으로 사용합니다.
 * 내부적으로는 username → user_id 변환, DAO 호출, 엔티티 ↔ DTO 매핑 로직을 수행합니다.
 */
public class MemoService {
    private final MemoDAO memoDAO;
    private final UserDAO userDAO;

    public MemoService(MemoDAO memoDAO, UserDAO userDAO) {
        this.memoDAO = memoDAO;
        this.userDAO = userDAO;
    }

    /**
     * 1) 메모 생성 또는 수정 (Upsert 방식)
     *
     * @param reqDto  PostMemoReqDto(ownerUsername, friendUsername, createdDate, content)
     * @return        PostMemoResDto(message, memoId, createdDate)
     */
    public PostMemoRes saveOrUpdateMemo(PostMemoReq reqDto) throws SQLException {
        // 1) username → user_id 조회
        Long ownerId = userDAO.findUserIdByUsername(reqDto.getOwnerUsername());
        Long friendId = userDAO.findUserIdByUsername(reqDto.getFriendUsername());

        if (ownerId == null) {
            throw new IllegalArgumentException("등록되지 않은 ownerUsername: " + reqDto.getOwnerUsername());
        }
        if (friendId == null) {
            throw new IllegalArgumentException("등록되지 않은 friendUsername: " + reqDto.getFriendUsername());
        }

        // 2) 기존 메모 존재 여부 조회
        Memo existing = memoDAO.findByOwnerFriendDate(ownerId, friendId, reqDto.getCreatedDate());
        if (existing == null) {
            // INSERT
            Memo newMemo = new Memo();
            newMemo.setOwnerId(ownerId);
            newMemo.setFriendId(friendId);
            newMemo.setCreatedAt(reqDto.getCreatedDate());
            newMemo.setContent(reqDto.getContent());
            Memo saved = memoDAO.insertMemo(newMemo);

            // 응답 DTO 생성
            PostMemoRes resDto = new PostMemoRes();
            resDto.setMessage("메모 생성 성공");
            resDto.setMemoId(saved.getMemoId());
            resDto.setCreatedDate(saved.getCreatedAt());
            return resDto;
        } else {
            // UPDATE (content만 교체)
            memoDAO.updateMemoContent(ownerId, friendId, reqDto.getCreatedDate(), reqDto.getContent());
            // 기존 existing 엔티티의 내용만 덮어씌움
            existing.setContent(reqDto.getContent());

            // 응답 DTO 생성
            PostMemoRes resDto = new PostMemoRes();
            resDto.setMessage("메모 수정 성공");
            resDto.setMemoId(existing.getMemoId());
            resDto.setCreatedDate(existing.getCreatedAt());
            return resDto;
        }

        // ★ MySQL Upsert(ON DUPLICATE KEY UPDATE)를 쓰려면 아래 코드를 대신 사용 가능
        // Memo m = new Memo();
        // m.setOwnerId(ownerId);
        // m.setFriendId(friendId);
        // m.setCreatedDate(reqDto.getCreatedDate());
        // m.setContent(reqDto.getContent());
        // memoDAO.upsertMemo(m);
        // // Upsert 후 INSERT/UPDATE 여부를 알 수 없으므로, 다시 조회해서 반환
        // Memo savedOrUpdated = memoDAO.findByOwnerFriendDate(ownerId, friendId, reqDto.getCreatedDate());
        // PostMemoResDto resDto = new PostMemoResDto();
        // resDto.setMessage("메모 저장(Upsert) 성공");
        // resDto.setMemoId(savedOrUpdated.getMemoId());
        // resDto.setCreatedDate(savedOrUpdated.getCreatedDate());
        // return resDto;
    }

    /**
     * 2) 특정 날짜 메모 조회
     *
     * @param reqDto  GetMemoReqDto(ownerUsername, friendUsername, createdDate)
     * @return        GetMemoResDto(content, createdDate)
     */
    public GetMemoRes getMemo(GetMemoReq reqDto) throws SQLException {
        // 1) username → user_id 조회
        Long ownerId = userDAO.findUserIdByUsername(reqDto.getOwnerUsername());
        Long friendId = userDAO.findUserIdByUsername(reqDto.getFriendUsername());

        // 만약 어느 username이든 존재하지 않으면 “메모 없음” 처리 (null 반환)
        if (ownerId == null || friendId == null) {
            return null;
        }

        // 2) DAO에서 엔티티 조회
        Memo found = memoDAO.findByOwnerFriendDate(ownerId, friendId, reqDto.getCreatedDate());
        if (found == null) {
            // 해당 날짜의 메모가 없으면 content를 빈 문자열로 리턴
            GetMemoRes resDto = new GetMemoRes();
            resDto.setContent("");
            resDto.setCreatedDate(reqDto.getCreatedDate());
            return resDto;
        }

        // 3) 엔티티 → DTO 매핑
        GetMemoRes resDto = new GetMemoRes();
        resDto.setContent(found.getContent());
        resDto.setCreatedDate(found.getCreatedAt());
        return resDto;
    }

    /**
     * 3) 특정 날짜 메모 삭제
     *
     * @param reqDto  GetMemoReqDto(ownerUsername, friendUsername, createdDate)
     * @return        삭제 성공 시 true, 없거나 error 시 false
     */
    public boolean deleteMemo(GetMemoReq reqDto) throws SQLException {
        // 1) username → user_id 조회
        Long ownerId = userDAO.findUserIdByUsername(reqDto.getOwnerUsername());
        Long friendId = userDAO.findUserIdByUsername(reqDto.getFriendUsername());

        if (ownerId == null || friendId == null) {
            return false;
        }
        return memoDAO.deleteMemo(ownerId, friendId, reqDto.getCreatedDate());
    }
}
