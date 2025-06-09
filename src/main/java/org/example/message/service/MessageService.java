// MessageService.java
package org.example.message.service;

import org.example.chat.service.ChatService;
import org.example.message.dao.MessageDAO;
import org.example.message.dto.MessageInfo;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;
import org.example.message.entity.Message;
import org.example.user.dao.UserDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 비즈니스 로직: 메시지 저장 및 조회
 */
public class MessageService {
    private final MessageDAO messageDAO;
    private final ChatService chatService;

    public MessageService(MessageDAO messageDAO, ChatService chatService) {
        this.messageDAO = messageDAO;
        this.chatService = chatService;
    }

    /**
     * 메시지를 저장하고, 응답 DTO로 반환
     */
    public SendMessageRes saveMessage(SendMessageReq req) throws SQLException {
        // 방 존재 및 멤버 권한 확인
        if (!chatService.existsRoom(req.getRoomId())) {
            throw new SQLException("Chat room not found: " + req.getRoomId());
        }
        if (!chatService.isActiveMember(req.getRoomId(), req.getSenderId())) {
            throw new SQLException("User " + req.getSenderId() + " is not active in chat room " + req.getRoomId());
        }

        // 엔티티 생성
        Message msg = new Message();
        msg.setRoomId(req.getRoomId());
        msg.setSenderId(req.getSenderId());
        msg.setContents(req.getContents());
        msg.setCreatedAt(LocalDateTime.now());

        // 저장
        Message saved = messageDAO.save(msg);

        // DTO 변환
        return new SendMessageRes(saved);
    }

    /**
     * 특정 채팅방에 사용자가 가입 시점 이후의 메시지 목록 조회
     * @param roomId 채팅방 ID
     * @param username 사용자 이름
     */
    public List<MessageInfo> fetchMessages(Long roomId, String username) throws SQLException {
        // 방 존재 확인
        if (!chatService.existsRoom(roomId)) {
            throw new SQLException("Chat room not found: " + roomId);
        }
        // 사용자별 가입 이후 메시지 조회
        return messageDAO.selectChatHistorySinceJoin(roomId, username);
    }
}