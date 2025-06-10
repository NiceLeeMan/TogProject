package org.example.message.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
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
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.Collections;
import java.util.Properties;
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

@ServerEndpoint("/ws/chat")
public class MessageController {

    // chatRoomId → 해당 방에 연결된 WebSocket 세션들 집합(Map)
    private static final ConcurrentHashMap<Long, Set<Session>> roomSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final MessageService messageService; // 외부에서 주입

    public MessageController() {
        System.out.println("⚡ CTOR 진입");
        try {
            // 1) db.properties 로딩
            Properties props = new Properties();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (is == null) {
                    throw new RuntimeException("db.properties 파일을 찾을 수 없습니다.");
                }
                props.load(is);
            }

            // 2) HikariCP 설정
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("jdbc.url"));
            config.setUsername(props.getProperty("jdbc.username"));
            config.setPassword(props.getProperty("jdbc.password"));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            HikariDataSource ds = new HikariDataSource(config);
            System.out.println("HikariDataSource 생성 완료");

            // 3) 서비스 생성
            ChatService chatService = new ChatService(new ChatDAO(ds));
            this.messageService = new MessageService(new MessageDAO(ds), chatService);
            System.out.println(" MessageService 생성 완료");
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(" MessageController 생성자 실패", t);
        }
    }

    // 2) 기본 생성자: 과제 환경에서 바로 동작하도록 DEFAULT_SERVICE 사용

    @OnOpen
    public void onOpen(Session session) {
        Long chatRoomId = extractChatRoomId(session);
        if (chatRoomId == null) {
            sendError(session, "chatRoomId 파라미터가 없습니다.");
            return;
        }

        System.out.println("onOpen, chatRoomId = " + chatRoomId);

        roomSessions
                .computeIfAbsent(chatRoomId, id -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    @OnMessage
    public void onMessage(Session session, String messageJson) throws IOException {
        Long chatRoomId = extractChatRoomId(session);
        System.out.println("onMessage, chatRoomId = " + chatRoomId);

        try {
            SendMessageReq req = objectMapper.readValue(messageJson, SendMessageReq.class);
            SendMessageRes res = messageService.saveMessage(req);
            String resJson = objectMapper.writeValueAsString(res);

            roomSessions.getOrDefault(chatRoomId, Collections.emptySet())
                    .stream()
                    .filter(Session::isOpen)
                    .forEach(s -> s.getAsyncRemote().sendText(resJson));
        } catch (SQLException e) {
            sendError(session, e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long chatRoomId = extractChatRoomId(session);
        System.out.println("onClose, chatRoomId = " + chatRoomId);

        Set<Session> sessions = roomSessions.get(chatRoomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(chatRoomId);
            }
        }
    }

    // 공통 함수
    private Long extractChatRoomId(Session session) {
        try {
            String query = session.getQueryString(); // "chatRoomId=56"
            if (query == null || !query.contains("chatRoomId=")) return null;
            return Long.parseLong(query.split("=")[1]);
        } catch (Exception e) {
            return null;
        }
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket Error: " + throwable.getMessage());
    }

    private void sendError(Session session, String errorMsg) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(
                        Collections.singletonMap("error", errorMsg)
                );
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
