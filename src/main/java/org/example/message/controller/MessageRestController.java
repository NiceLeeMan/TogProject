package org.example.message.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.chat.dao.ChatDAO;
import org.example.chat.service.ChatService;
import org.example.message.dao.MessageDAO;
import org.example.message.dto.*;
import org.example.message.service.MessageService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
        * HTTP 요청을 처리하는 MessageRestController
 * - POST /api/messages/send    → handleSendMessage
 * - GET  /api/messages         → handleGetMessageHistory
 */

public class MessageRestController extends HttpServlet {

    private MessageService messageService;
    private ObjectMapper objectMapper;

    public MessageRestController() {
        super();
    }

    public MessageRestController(MessageService messageService) {
        this.messageService = messageService;
        // 테스트에서는 간단하게 ObjectMapper 기본 설정만 해 줍니다.
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Override
    public void init() throws ServletException {
        super.init();

        // 1) ObjectMapper 초기화
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2) db.properties 로드
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new ServletException("db.properties 파일을 찾을 수 없습니다.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new ServletException("db.properties 로딩 실패", e);
        }

        System.out.println("[init] jdbc.url = " + props.getProperty("jdbc.url"));

        // 3) HikariConfig에 JDBC 정보 주입
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("jdbc.url"));
        config.setUsername(props.getProperty("jdbc.username"));
        config.setPassword(props.getProperty("jdbc.password"));
        // driverClassName 은 생략 가능

        DataSource ds;
        try {
            ds = new HikariDataSource(config);
            System.out.println("[init] DataSource = " + ds);
        } catch (Exception e) {
            throw new ServletException("HikariDataSource 생성 실패", e);
        }
        System.out.println("DataSource 초기화 성공: " + ds);

        // 4) Service 계층 초기화
        ChatService chatService = new ChatService(new ChatDAO(ds));
        this.messageService = new MessageService(new MessageDAO(ds), chatService);
    }



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(">> Incoming POST");
        System.out.println("   servletPath: " + req.getServletPath());
        System.out.println("   pathInfo   : " + req.getPathInfo());
        resp.setContentType("application/json; charset=UTF-8");

        String path = req.getPathInfo(); // expected "/send"
        System.out.println("path: " + path);

        try {
            if ("/send".equals(path)) {
                handleSendMessage(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "지원하지 않는 경로: POST " + path);
            }
        } catch (Exception e) {
            // 예외 스택트레이스와 메시지 모두 찍기
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(
                    new ObjectMapper().writeValueAsString(
                            Map.of("error", e.getClass().getSimpleName(),
                                    "message", e.getMessage())
                    )
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo(); // expected null or "/"
        System.out.println("path: " + path);
        if (path == null || "/".equals(path)) {
            handleGetMessageHistory(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "지원하지 않는 경로: GET " + path);
        }
    }

    private void handleSendMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SendMessageReq sendReq = objectMapper.readValue(req.getInputStream(), SendMessageReq.class);
        System.out.println("sendReq: " + sendReq);
        try {
            SendMessageRes sendRes = messageService.saveMessage(sendReq);
            System.out.println("sendRes: " + sendRes);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = resp.getWriter()) {
                objectMapper.writeValue(out, sendRes);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleGetMessageHistory(HttpServletRequest req,
                                         HttpServletResponse resp) throws IOException {
        // 1) 로그 찍어보기
        System.out.printf("[handleGetMessageHistory] roomId=%s, username=%s%n",
                req.getParameter("roomId"),
                req.getParameter("username"));

        // 2) 파라미터 유효성 검사
        String roomIdParam = req.getParameter("roomId");
        String username    = req.getParameter("username");
        if (roomIdParam == null || username == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing roomId or username parameter");
            return;
        }

        // 3) 타입 변환
        Long roomId = Long.valueOf(roomIdParam);
        System.out.println("roomId: " + roomId);

        try {
            // 4) 서비스 호출
            List<MessageInfo> messages = messageService.fetchMessages(roomId, username);

            // 5) 응답 작성
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");

            System.out.println("messages: " + messages);
            try (PrintWriter out = resp.getWriter()) {
                objectMapper.writeValue(out, messages);
            }
        } catch (SQLException e) {
            // DB 에러는 500으로
            System.err.println("[ERROR] " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
