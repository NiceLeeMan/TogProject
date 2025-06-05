package org.example.message.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;
import org.example.message.service.MessageService;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 엔드포인트: 채팅방별 브로드캐스트 담당
 *
 * 클라이언트가 접속할 때 URL에 chatRoomId를 붙여서 연결합니다.
 * 예) ws://서버주소:포트/app/ws/chat/123
 *
 * 1) @OnOpen: 채팅방 ID별로 Session을 보관
 * 2) @OnMessage: 메시지가 들어오면 저장 → 같은 방 세션 전부에 broadcast
 * 3) @OnClose: 연결 끊길 때 roomSessions에서 해당 Session 제거
 */

@ServerEndpoint("/ws/chat/{chatRoomId}")
public class MessageController {

    // chatRoomId → 해당 방에 연결된 WebSocket 세션들 집합(Map)
    private static final ConcurrentHashMap<Long, Set<Session>> roomSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static MessageService messageService; // 외부에서 주입

    // 애플리케이션 초기화 시점(예: ServletContextListener)에서 이 메서드로 주입
    public static void setMessageService(MessageService svc) {
        messageService = svc;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("chatRoomId") Long chatRoomId) {
        // chatRoomId에 대응하는 Set<Session>이 없으면 새로 만들고, 세션 추가
        roomSessions
                .computeIfAbsent(chatRoomId, __ -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    @OnMessage
    public void onMessage(Session session,
                          String messageJson,
                          @PathParam("chatRoomId") Long chatRoomId) {
        try {
            // 1) 클라이언트가 보낸 JSON을 SendMessageReq로 역직렬화
            SendMessageReq reqDto = objectMapper.readValue(messageJson, SendMessageReq.class);

            // 2) MessageService를 통해 DB에 저장 (message_id, createdAt 포함된 SendMessageRes 반환)
            SendMessageRes resDto = messageService.saveMessage(reqDto);

            // 3) 같은 채팅방에 접속된 모든 세션에 SendMessageRes(JSON) 브로드캐스트
            String resJson = objectMapper.writeValueAsString(resDto);
            Set<Session> sessions = roomSessions.get(chatRoomId);
            if (sessions != null) {
                for (Session s : sessions) {
                    if (s.isOpen()) {
                        try {
                            s.getBasicRemote().sendText(resJson);
                        } catch (IOException e) {
                            // 개별 세션 전송 실패 시 로그만 남기고 계속
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 예외 처리: JSON 파싱 오류, DB 오류 등
            e.printStackTrace();
            sendError(session, "메시지 처리 중 오류: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("chatRoomId") Long chatRoomId) {
        Set<Session> sessions = roomSessions.get(chatRoomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(chatRoomId);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("chatRoomId") Long chatRoomId) {
        // WebSocket 에러 발생 시 로그
        throwable.printStackTrace();
    }

    private void sendError(Session session, String errorMsg) {
        try {
            String json = objectMapper.writeValueAsString(
                    java.util.Collections.singletonMap("error", errorMsg)
            );
            if (session.isOpen()) {
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
