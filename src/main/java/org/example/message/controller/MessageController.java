package org.example.message.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariDataSource;
import org.example.chat.dao.ChatDAO;
import org.example.chat.service.ChatService;
import org.example.config.DataSoruceConfig;
import org.example.message.dao.MessageDAO;
import org.example.message.dto.*;
import org.example.message.service.MessageService;
import jakarta.websocket.*;
import jakarta.websocket.Session;                       // ← 반드시 jakarta.websocket.Session
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
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
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final MessageService messageService; // 외부에서 주입

    // 1) 모킹·단위 테스트용 생성자 (테스트 코드에서 이걸 호출)
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    public MessageController() {

        HikariDataSource ds = DataSoruceConfig.getDataSource();

        ChatDAO chatDao   = new ChatDAO(ds);
        ChatService chatSvc = new ChatService(chatDao);
        MessageDAO  msgDao = new MessageDAO(ds);
        this.messageService = new MessageService(msgDao, chatSvc);
    }

    // 2) 기본 생성자: 과제 환경에서 바로 동작하도록 DEFAULT_SERVICE 사용

    @OnOpen
    public void onOpen(Session session, @PathParam("chatRoomId") Long chatRoomId) {
        // chatRoomId에 대응하는 Set<Session>이 없으면 새로 만들고, 세션 추가
        roomSessions
                .computeIfAbsent(chatRoomId, __ -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    @OnMessage
    public void onMessage(Session session,
                          @PathParam("chatRoomId") Long chatRoomId,
                          String messageJson) throws IOException, SQLException {
        // 1) 요청 DTO
        SendMessageReq req = objectMapper.readValue(messageJson, SendMessageReq.class);

        // 2) 바로 응답 DTO 반환
        SendMessageRes res = messageService.saveMessage(req);

        // 3) JSON 직렬화 + 비동기 브로드캐스트
        String resJson = objectMapper.writeValueAsString(res);
        roomSessions.getOrDefault(chatRoomId, Collections.emptySet())
                .stream()
                .filter(Session::isOpen)
                .forEach(s -> s.getAsyncRemote().sendText(resJson));
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
