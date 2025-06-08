// MessageService.java
package org.example.message.service;

import org.example.chat.service.ChatService;
import org.example.message.dao.MessageDAO;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;
import org.example.message.entity.Message;
import org.example.user.dao.UserDAO;

import java.sql.SQLException;

public class MessageService {
    private final MessageDAO messageDAO;
    private final ChatService chatService;

    public MessageService(MessageDAO messageDAO, ChatService chatService) {
        this.messageDAO = messageDAO;
        this.chatService = chatService;
    }

    // 변경된 시그니처: 엔티티 → DTO 반환
    public SendMessageRes saveMessage(SendMessageReq req) throws SQLException {
        Message toSave = new Message(
                null,
                req.getRoomId(),
                req.getSenderId(),
                req.getContents(),
                null
        );
        Message saved = messageDAO.save(toSave);
        return new SendMessageRes(saved);
    }
}