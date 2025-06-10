package org.example.chat.controller;

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
import org.example.chat.dto.GetRoom.GetRoomsReq;
import org.example.chat.dto.GetRoom.GetRoomsRes;
import org.example.chat.dto.Info.RoomInfo;
import org.example.chat.dto.outDto.OutChatRoomReqDto;
import org.example.chat.dto.outDto.OutChatRoomResDto;
import org.example.chat.service.ChatService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * ChatController
 *
 * • HTTP 요청을 받아 ChatService를 호출하고, JSON 응답을 반환합니다.
 * • 엔드포인트:
 *   POST /chat/one-to-one/create   → createOneToOneChat
 *   POST /chat/group/create        → createGroupChat
 *   POST /chat/join                → joinChat
 *   POST /chat/leave               → leaveChat
 *   POST /chat/send                → sendMessage (1:1 재가입용)
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

                case "/rooms":
                    handleGetJoinedRoom(request, response);
                    break;

                case "/group/create":
                    handleCreateGroup(request, response);
                    break;
                case "/join":
                    handleJoinChat(request, response);
                    break;
                case "/leave":
                    handleLeaveChat(request, response);
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

    private void handleJoinChat(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        // 요청 JSON → JoinChatReqDto
        JoinChatReqDto reqDto = objectMapper.readValue(request.getInputStream(), JoinChatReqDto.class);


        // 2) 입력 검증: chatRoomId, username 둘 다 필수
        if (reqDto.getChatRoomId() == null || reqDto.getUsername() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                objectMapper.writeValue(out, new ErrorResponse(
                        "INVALID_REQUEST",
                        "chatRoomId and username are required"
                ));
            }
            return;
        }
        // 서비스 호출
        JoinChatResDto resDto = chatService.joinChat(reqDto);

        // 응답 JSON 쓰기
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            objectMapper.writeValue(out, resDto);
        }
    }

    private void handleLeaveChat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 요청 JSON → OutChatRoomReqDto
        OutChatRoomReqDto reqDto = objectMapper.readValue(request.getInputStream(), OutChatRoomReqDto.class);
        // 서비스 호출
        OutChatRoomResDto resDto;
        try {
            resDto = chatService.leaveChatRoom(reqDto);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                objectMapper.writeValue(out, new ErrorResponse("DB_ERROR", e.getMessage()));
            }
            return;
        }

        // 응답 JSON 쓰기
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            objectMapper.writeValue(out, resDto);
        }
    }

    private void handleGetJoinedRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 1) 요청 바디로부터 DTO 파싱
            GetRoomsReq reqDto = objectMapper.readValue(request.getReader(), GetRoomsReq.class);

            // 2) 서비스 호출
            List<RoomInfo> rooms = chatService.getChatRooms(reqDto.getUsername());
            System.out.println("rooms: "+rooms);

            // 3) 응답 DTO 생성
            GetRoomsRes resDto = new GetRoomsRes(rooms);

            // 4) JSON 직렬화 및 응답
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), resDto);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // DTO 파싱 오류 등 잘못된 요청
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            // 서비스, DAO 레벨 예외
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "채팅방 목록 조회 중 오류가 발생했습니다.");
        }
    }



}
