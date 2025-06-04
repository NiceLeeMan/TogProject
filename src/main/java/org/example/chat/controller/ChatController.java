package org.example.chat.controller;

// ChatController.java


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
import org.example.chat.dto.*;
import org.example.chat.service.ChatService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * ChatController
 *
 * • HTTP 요청을 받아 ChatService를 호출하고, JSON 응답을 반환합니다.
 * • 엔드포인트:
 *   POST /chat/one-to-one/create   → createOneToOneChat
 *   POST /chat/group/create        → createGroupChat
 *   POST /chat/join                → joinChat
 */

public class ChatController extends HttpServlet {

    private ChatService chatService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        // 1) db.properties 파일을 Resource Stream으로 읽어오기
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new ServletException("db.properties 파일을 찾을 수 없습니다.");
            }
            props.load(is);
        } catch (IOException e) {
            throw new ServletException("db.properties 로딩 실패", e);
        }

        // 2) HikariCP 설정
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("jdbc.url"));
        config.setUsername(props.getProperty("jdbc.username"));
        config.setPassword(props.getProperty("jdbc.password"));
        DataSource ds = new HikariDataSource(config);

        // 3) DAO, Service, ObjectMapper 초기화
        ChatDAO chatDAO = new ChatDAO(ds);
        this.chatService = new ChatService(chatDAO);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo(); // 예: "/one-to-one/create"
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            switch (path) {
                case "/one-to-one/create":
                    handleCreateOneToOne(request, response);
                    break;
                case "/group/create":
                    handleCreateGroup(request, response);
                    break;
                case "/join":
                    handleJoinChat(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    try (PrintWriter out = response.getWriter()) {
                        out.write("{\"error\":\"지원하지 않는 경로입니다.\"}");
                    }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                objectMapper.writeValue(out, new ErrorResponse("INTERNAL_ERROR", e.getMessage()));
            }
        }
    }

    private void handleCreateOneToOne(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 요청 JSON → CreateOneToOneChatReqDto
        CreateOneToOneChatReqDto reqDto = objectMapper.readValue(request.getInputStream(), CreateOneToOneChatReqDto.class);
        // 서비스 호출
        JoinChatResDto resDto = chatService.createAndJoinOneToOne(reqDto);
        // 응답 JSON 쓰기
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            objectMapper.writeValue(out, resDto);
        }
    }

    private void handleCreateGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 요청 JSON → CreateGroupChatReqDto
        CreateGroupChatReqDto reqDto = objectMapper.readValue(request.getInputStream(), CreateGroupChatReqDto.class);
        // 서비스 호출
        JoinChatResDto resDto = chatService.createAndJoinGroup(reqDto);
        // 응답 JSON 쓰기
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            objectMapper.writeValue(out, resDto);
        }
    }

    private void handleJoinChat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 요청 JSON → JoinChatReqDto
        JoinChatReqDto reqDto = objectMapper.readValue(request.getInputStream(), JoinChatReqDto.class);
        // 서비스 호출
        JoinChatResDto resDto = chatService.joinChat(reqDto);
        // 응답 JSON 쓰기
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            objectMapper.writeValue(out, resDto);
        }
    }

}
