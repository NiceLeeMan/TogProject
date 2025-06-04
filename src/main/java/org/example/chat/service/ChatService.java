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
     * 1) 1:1 채팅방 생성
     */
    public CreateOneToOneChatResDto createOneToOneChat(CreateOneToOneChatReqDto req) {
        try {
            String creatorUsername = req.getUsername();
            String friendUsername  = req.getFriendUsername();

            Long newRoomId = chatDAO.createOneToOneChat(creatorUsername, friendUsername);

            // 생성 직후 방에 속한 멤버 목록 조회
            List<MemberInfo> members = chatDAO.getChatRoomMembers(newRoomId);

            CreateOneToOneChatResDto res = new CreateOneToOneChatResDto(newRoomId);
            res.setMembers(members);
            res.setCreatedAt(LocalDateTime.now());
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return new CreateOneToOneChatResDto();
        }
    }

    /**
     * 2) 그룹 채팅방 생성
     */
    public CreateGroupChatResDto createGroupChat(CreateGroupChatReqDto req) {
        try {
            String creatorUsername   = req.getUsername();
            String chatRoomName      = req.getChatRoomName();
            List<String> memberUsernames = req.getMembers()
                    .stream()
                    .map(MemberInfo::getUsername)
                    .collect(Collectors.toList());

            Long newRoomId = chatDAO.createGroupChat(creatorUsername, chatRoomName, memberUsernames);

            List<MemberInfo> members = chatDAO.getChatRoomMembers(newRoomId);

            CreateGroupChatResDto res = new CreateGroupChatResDto(newRoomId, chatRoomName, members, LocalDateTime.now());
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return new CreateGroupChatResDto();
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