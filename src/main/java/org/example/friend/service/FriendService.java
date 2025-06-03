package org.example.friend.service;

// ────────────────────────────────────────────────────────────────────────────────
// File: FriendService.java
// Path: src/main/java/org/example/friend/service/FriendService.java
// ────────────────────────────────────────────────────────────────────────────────

import org.example.config.DataSoruceConfig;
import org.example.friend.dao.FriendDAO;
import org.example.friend.dto.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * FriendService
 *
 * • DAO에서 제공하는 SQL 기반 메서드를 호출하여
 *   비즈니스 로직 수준에서 예외를 처리하고
 *   각종 응답 DTO를 생성/반환한다.
 */
public class FriendService {

    private final FriendDAO friendDAO;

    public FriendService(FriendDAO friendDAO) {

        this.friendDAO = friendDAO;
    }
    /**
     * 1) 친구 목록 조회
     *
     * @param req GetFriendsListReqDto (username 필드 포함) :contentReference[oaicite:0]{index=0}
     * @return GetFriendsListResDto (FriendInfo 리스트 포함) :contentReference[oaicite:1]{index=1} :contentReference[oaicite:2]{index=2}
     */
    public GetFriendsListResDto getFriendsList(GetFriendsListReqDto req) {
        try {
            List<FriendInfo> friends = friendDAO.getFriendList(req.getUsername());
            return new GetFriendsListResDto(friends);
        } catch (SQLException e) {
            // 예외 발생 시 빈 리스트를 반환하거나, 필요하다면 별도 필드에 에러 메시지를 담아줘도 좋다.
            e.printStackTrace();
            return new GetFriendsListResDto();
        }
    }

    /**
     * 2) 친구 추가
     *
     * @param req AddFriendReqDto (username, friendUsername 포함) :contentReference[oaicite:3]{index=3}
     * @return AddFriendResDto (statusCode, 업데이트된 FriendInfo 리스트) :contentReference[oaicite:4]{index=4} :contentReference[oaicite:5]{index=5}
     */
    public AddFriendResDto addFriend(AddFriendReqDto req) {
        String username       = req.getUsername();
        String friendUsername = req.getFriendUsername();

        try {
            friendDAO.addFriend(username, friendUsername);  // :contentReference[oaicite:6]{index=6}
            List<FriendInfo> updatedList = friendDAO.getFriendList(username);
            return new AddFriendResDto("OK", updatedList);
        } catch (SQLException e) {
            String sqlMessage = e.getMessage();

            // ① 외래 키 제약 위반 (존재하지 않는 사용자) 예외 처리
            if (sqlMessage.contains("Cannot add or update a child row")
                    || sqlMessage.toLowerCase().contains("foreign key")) {
                return new AddFriendResDto("ERROR_NOT_FOUND", null);
            }
            // ② 중복 삽입 시 (unique key 위반) 예외 처리: schema에서 UNIQUE(user_id, friend_id) 제약이 있다고 가정
            if (sqlMessage.toLowerCase().contains("duplicate")) {
                return new AddFriendResDto("ERROR_ALREADY_FRIEND", null);
            }
            // ③ 기타 내부 에러
            e.printStackTrace();
            return new AddFriendResDto("ERROR_INTERNAL", null);
        }
    }

    /**
     * 3) 친구 삭제
     *
     * @param req RemoveFriendReqDto (username, friendUsername 포함) :contentReference[oaicite:7]{index=7}
     * @return RemoveFriendResDto (message, 업데이트된 FriendInfo 리스트) :contentReference[oaicite:8]{index=8} :contentReference[oaicite:9]{index=9}
     */
    public RemoveFriendResDto removeFriend(RemoveFriendReqDto req) {
        String username       = req.getUsername();
        String friendUsername = req.getFriendUsername();

        try {
            friendDAO.removeFriend(username, friendUsername);  // :contentReference[oaicite:10]{index=10}
            List<FriendInfo> updatedList = friendDAO.getFriendList(username);
            return new RemoveFriendResDto("삭제 성공", updatedList);
        } catch (SQLException e) {
            // 예외 발생 시 “ERROR_INTERNAL” 또는 상황에 맞는 메시지 반환
            e.printStackTrace();
            return new RemoveFriendResDto("ERROR_INTERNAL", null);
        }
    }
}
