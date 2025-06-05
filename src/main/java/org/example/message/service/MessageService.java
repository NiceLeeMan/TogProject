// MessageService.java
package org.example.message.service;

import org.example.message.dao.MessageDAO;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;
import org.example.message.entity.Message;
import org.example.user.dao.UserDAO;

import java.sql.SQLException;

public class MessageService {
    private final MessageDAO messageDAO;
    private final UserDAO userDAO;

    public MessageService(MessageDAO messageDAO, UserDAO userDAO) {
        this.messageDAO = messageDAO;
        this.userDAO = userDAO;
    }

    /**
     * 1) 메시지 저장 로직
     *
     * @param reqDto SendMessageReq(senderUsername, chatRoomId, content, sentAt)
     * @return       SendMessageRes(messageId, chatRoomId, senderUsername, content, createdAt)
     */
    public SendMessageRes saveMessage(SendMessageReq reqDto) throws SQLException {
        // ① username → user_id 조회
        Long senderId = userDAO.findUserIdByUsername(reqDto.getSenderUsername());
        if (senderId == null) {
            throw new IllegalArgumentException("등록되지 않은 senderUsername: " + reqDto.getSenderUsername());
        }

        // ② DTO → 엔티티 매핑
        Message msgEntity = new Message();
        msgEntity.setRoomId(reqDto.getChatRoomId());
        msgEntity.setSenderId(senderId);
        msgEntity.setContents(reqDto.getContent());
        // createdAt은 DAO에서 DB 값으로 채워줌

        // ③ DAO 호출 (엔티티 전달)
        Message savedEntity = messageDAO.insertMessage(msgEntity);

        // ④ 엔티티 → DTO 매핑
        SendMessageRes resDto = new SendMessageRes();
        resDto.setMessageId(savedEntity.getMsgId());
        resDto.setChatRoomId(savedEntity.getRoomId());
        resDto.setSenderUsername(reqDto.getSenderUsername());
        resDto.setContent(savedEntity.getContents());
        resDto.setCreatedAt(savedEntity.getCreatedAt());

        return resDto;
    }
}
