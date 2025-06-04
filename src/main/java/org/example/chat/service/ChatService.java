package org.example.chat.service;

import org.example.chat.dao.ChatDAO;
import org.example.chat.dto.*;
import org.example.message.dto.MessageInfo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatService
 *
 * • ChatDAO를 호출하여 비즈니스 로직을 처리하고, DTO로 결과를 반환합니다.
 */
public class ChatService {

    private final ChatDAO chatDAO;

    public ChatService(ChatDAO chatDAO) {
        this.chatDAO = chatDAO;
    }

    /**
     * 1:1 채팅방 생성 + 바로 입장 정보 반환
     */
    public JoinChatResDto createAndJoinOneToOne(CreateOneToOneChatReqDto req) {
        try {
            // 1) 방 생성 (DB 상으로 생성자+대상 모두 MEMBER 테이블에 INSERT됨)
            Long newRoomId = chatDAO.createOneToOneChat(
                    req.getUsername(),
                    req.getFriendUsername()
            );

            // 2) 방 이름 조회
            String roomName = chatDAO.findRoomNameById(newRoomId);

            // 3) 멤버 정보 조회 (joinedAt 포함, 생성자는 이미 참여되어 있음)
            List<MemberInfo> members = chatDAO.getChatRoomMembers(newRoomId);

            // 4) 메시지 내역 조회 (생성 직후라 메시지는 없겠지만, API 형태 통일)
            List<MessageInfo> messages = chatDAO.getChatHistory(newRoomId, req.getUsername());

            return new JoinChatResDto(newRoomId, roomName, members, messages);
        } catch (SQLException e) {
            e.printStackTrace();
            return new JoinChatResDto(); // 빈 DTO 리턴
        }
    }

    /**
     * 그룹 채팅방 생성 + 바로 입장 정보 반환
     */
    public JoinChatResDto createAndJoinGroup(CreateGroupChatReqDto req) {
        try {
            Long newRoomId = chatDAO.createGroupChat(
                    req.getUsername(),
                    req.getChatRoomName(),
                    req.getMembers().stream()
                            .map(MemberInfo::getUsername)
                            .collect(Collectors.toList())
            );

            String roomName = chatDAO.findRoomNameById(newRoomId);
            List<MemberInfo> members = chatDAO.getChatRoomMembers(newRoomId);
            // 생성 직후이므로 메시지는 없겠지만, 구조적으로 가져오기
            List<MessageInfo> messages = chatDAO.getChatHistory(newRoomId, req.getUsername());

            return new JoinChatResDto(newRoomId, roomName, members, messages);
        } catch (SQLException e) {
            e.printStackTrace();
            return new JoinChatResDto();
        }
    }

    /**
     * 3) 채팅방 입장 (클릭해서 방 들어가기) — 단순히 조회만 수행
     */
    public JoinChatResDto joinChat(JoinChatReqDto req) {
        try {
            Long chatRoomId = req.getChatRoomId();
            String username = req.getUsername();

            // 채팅방 이름 조회
            String roomName = chatDAO.findRoomNameById(chatRoomId);
            // 멤버 목록 조회 (joinedAt 포함)
            List<MemberInfo> members = chatDAO.getChatRoomMembers(chatRoomId);
            // 메시지 내역 조회 (입장 시점 이후)
            List<MessageInfo> messages = chatDAO.getChatHistory(chatRoomId, username);

            return new JoinChatResDto(chatRoomId, roomName, members, messages);

        } catch (SQLException e) {
            e.printStackTrace();
            return new JoinChatResDto();
        }
    }
}