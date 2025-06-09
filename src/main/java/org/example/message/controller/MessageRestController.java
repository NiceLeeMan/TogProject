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
import java.util.Properties;

/**
        * HTTP 요청을 처리하는 MessageRestController
 * - POST /api/messages/send    → handleSendMessage
 * - GET  /api/messages         → handleGetMessageHistory
 */

public class MessageRestController extends HttpServlet {

    private MessageService messageService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        // ObjectMapper 설정
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // DataSource, Service 초기화
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new ServletException("db.properties 로딩 실패", e);
        }

        HikariConfig config = new HikariConfig();
        DataSource ds = new HikariDataSource(config);
        ChatService chatService = new ChatService(new ChatDAO(ds));
        this.messageService = new MessageService(new MessageDAO(ds), chatService);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo(); // expected "/send"
        if ("/send".equals(path)) {
            handleSendMessage(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "지원하지 않는 경로: POST " + path);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String path = req.getPathInfo(); // expected null or "/"
        if (path == null || "/".equals(path)) {
            handleGetMessageHistory(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "지원하지 않는 경로: GET " + path);
        }
    }

    private void handleSendMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SendMessageReq sendReq = objectMapper.readValue(req.getInputStream(), SendMessageReq.class);
        try {
            SendMessageRes sendRes = messageService.saveMessage(sendReq);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter out = resp.getWriter()) {
                objectMapper.writeValue(out, sendRes);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleGetMessageHistory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String roomIdParam = req.getParameter("roomId");
        String username = req.getParameter("username");
        if (roomIdParam == null || username == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing roomId or username parameter");
            return;
        }
        Long roomId = Long.valueOf(roomIdParam);
        try {
            List<MessageInfo> messages = messageService.fetchMessages(roomId, username);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = resp.getWriter()) {
                objectMapper.writeValue(out, messages);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
