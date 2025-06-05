package org.example.memo.service;

import org.example.memo.dao.MemoDAO;
import org.example.memo.dto.*;
import org.example.memo.entity.Memo;
import org.example.user.dao.UserDAO;

import java.sql.SQLException;

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
     * @param reqDto PostMemoReq(ownerUsername, friendUsername, createdDate, content)
     * @return       PostMemoRes(message, memoId, createdDate)
     */
    public PostMemoRes saveOrUpdateMemo(PostMemoReq reqDto) throws SQLException {
        // 1) username → user_id 조회
        Long ownerId  = userDAO.findUserIdByUsername(reqDto.getOwnerUsername());
        Long friendId = userDAO.findUserIdByUsername(reqDto.getFriendUsername());

        if (ownerId == null) {
            throw new IllegalArgumentException("등록되지 않은 ownerUsername: " + reqDto.getOwnerUsername());
        }
        if (friendId == null) {
            throw new IllegalArgumentException("등록되지 않은 friendUsername: " + reqDto.getFriendUsername());
        }

        // 2) DTO → 엔티티 매핑 (조회용)
        Memo lookupMemo = new Memo();
        lookupMemo.setOwnerId(ownerId);
        lookupMemo.setFriendId(friendId);
        lookupMemo.setCreatedAt(reqDto.getCreatedDate());

        // 3) 기존 메모 존재 여부 조회
        Memo existing = memoDAO.findByOwnerFriendDate(lookupMemo);
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
            existing.setContent(reqDto.getContent());
            memoDAO.updateMemoContent(existing);

            // 응답 DTO 생성
            PostMemoRes resDto = new PostMemoRes();
            resDto.setMessage("메모 수정 성공");
            resDto.setMemoId(existing.getMemoId());
            resDto.setCreatedDate(existing.getCreatedAt());
            return resDto;
        }
    }

    /**
     * 2) 특정 날짜 메모 조회
     *
     * @param reqDto GetMemoReq(ownerUsername, friendUsername, createdDate)
     * @return       GetMemoRes(content, createdDate)
     */
    public GetMemoRes getMemo(GetMemoReq reqDto) throws SQLException {
        // 1) username → user_id 조회
        Long ownerId  = userDAO.findUserIdByUsername(reqDto.getOwnerUsername());
        Long friendId = userDAO.findUserIdByUsername(reqDto.getFriendUsername());

        if (ownerId == null || friendId == null) {
            return null;
        }

        // 2) DTO → 엔티티 매핑 (조회용)
        Memo lookupMemo = new Memo();
        lookupMemo.setOwnerId(ownerId);
        lookupMemo.setFriendId(friendId);
        lookupMemo.setCreatedAt(reqDto.getCreatedDate());

        // 3) DAO 호출
        Memo found = memoDAO.findByOwnerFriendDate(lookupMemo);

        // 4) 엔티티 → DTO 매핑
        GetMemoRes resDto = new GetMemoRes();
        if (found == null) {
            resDto.setContent("");
            resDto.setCreatedDate(reqDto.getCreatedDate());
        } else {
            resDto.setContent(found.getContent());
            resDto.setCreatedDate(found.getCreatedAt());
        }
        return resDto;
    }

    /**
     * 3) 특정 날짜 메모 삭제
     *
     * @param reqDto GetMemoReq(ownerUsername, friendUsername, createdDate)
     * @return       삭제 성공 시 true, 없거나 오류 시 false
     */
    public boolean deleteMemo(GetMemoReq reqDto) throws SQLException {
        // 1) username → user_id 조회
        Long ownerId  = userDAO.findUserIdByUsername(reqDto.getOwnerUsername());
        Long friendId = userDAO.findUserIdByUsername(reqDto.getFriendUsername());

        if (ownerId == null || friendId == null) {
            return false;
        }

        // 2) DTO → 엔티티 매핑 (삭제용)
        Memo deleteMemo = new Memo();
        deleteMemo.setOwnerId(ownerId);
        deleteMemo.setFriendId(friendId);
        deleteMemo.setCreatedAt(reqDto.getCreatedDate());

        // 3) DAO 호출
        return memoDAO.deleteMemo(deleteMemo);
    }
}
