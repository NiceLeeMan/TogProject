package org.example.chat.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.example.chat.dto.GetRoom.GetRoomsRes;
import org.example.chat.dto.Info.RoomInfo;
import org.example.chat.dto.outDto.OutChatRoomReqDto;
import org.example.chat.dto.outDto.OutChatRoomResDto;
import org.example.chat.service.ChatService;
import org.example.config.TestApiConfig;
import org.example.message.dao.MessageDAO;
import org.example.message.dto.MessageInfo;
import org.example.message.service.MessageService;

import javax.sql.DataSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
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
    private MessageService messageService;

    @Override
    public void init() throws ServletException {
        super.init();
        // 1) db.properties 파일을 Resource Stream으로 읽어오기
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/db.properties")) {
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
        MessageDAO messageDAO = new MessageDAO(ds);
        this.chatService = new ChatService(chatDAO);
        this.messageService = new MessageService(messageDAO, this.chatService);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        System.out.println("path : " + path);// 예: "/one-to-one/create"
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        System.out.println("분기문 진입");
        try {
            switch (path) {
                case "/one-to-one/create":
                    handleCreateOneToOne(request, response);
                    break;

                case "/rooms":
                    System.out.println("룸 분기 진입");
                    handleGetJoinedRoom(request, response);
                    break;

                case "/group/create":
                    handleCreateGroup(request, response);
                    break;

                case "/join":
                    System.out.println("join분기문 진입");
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            if ("/rooms".equals(path)) {
                handleGetJoinedRoom(request, response);
            }
            else if ("/messages".equals(path)) {
                // 메시지 조회 처리
                try {
                    handleFetchMessages(request, response);
                } catch (SQLException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    objectMapper.writeValue(out, new ErrorResponse("DB_ERROR", e.getMessage()));
                }
            }
            else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"지원하지 않는 GET 경로입니다.\"}");
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
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

    private void handleJoinChat(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        JoinChatReqDto reqDto = objectMapper.readValue(request.getInputStream(), JoinChatReqDto.class);
        if (reqDto.getChatRoomId() == null || reqDto.getUsername() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                objectMapper.writeValue(out,
                        new ErrorResponse("INVALID_REQUEST", "chatRoomId and username are required")
                );
            }
            return;
        }

        // 1) 가입 처리 (이때 JoinChatResDto.members가 채워져 반환됩니다)
        JoinChatResDto resDto = chatService.joinChat(reqDto);

        // 2) 메시지 이력 조회
        String fetchUrl = TestApiConfig.get("api.baseUrl")
                + TestApiConfig.get("api.messages.fetch")
                + "?chatRoomId="  + reqDto.getChatRoomId()
                + "&username="    + URLEncoder.encode(reqDto.getUsername(), StandardCharsets.UTF_8);
        HttpResponse fetchRes = sendGet(fetchUrl);
        System.out.println("[handleJoinChat] Fetch URL -> " + fetchUrl);
        List<MessageInfo> history = Collections.emptyList();
        if (fetchRes.statusCode == 200 && !fetchRes.body.isEmpty()) {
            history = objectMapper.readValue(
                    fetchRes.body,
                    new TypeReference<List<MessageInfo>>() {}
            );
        }
        resDto.setMessages(history);

        // 3) 최종 응답
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
            System.out.println("resDto(leaveChatRoom) = " + resDto);
        } catch (SQLException e) {
            e.printStackTrace();
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
        // 1) 쿼리 파라미터에서 username 추출
        String username = request.getParameter("username");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username 파라미터가 필요합니다.");
        }

        // 2) 서비스 호출
        List<RoomInfo> rooms = chatService.getChatRooms(username);

        // 3) 응답 DTO 생성
        GetRoomsRes resDto = new GetRoomsRes(rooms);

        // 4) JSON 직렬화 및 응답
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(resDto));
    }
    private void handleFetchMessages(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Long roomId = Long.valueOf(request.getParameter("chatRoomId"));
        String username = request.getParameter("username");

        List<MessageInfo> messages = messageService.fetchMessages(roomId, username);
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), messages);
    }
    // --------------------------------------------------
// HTTP GET 요청 헬퍼
// --------------------------------------------------
    private static HttpResponse sendGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        String body = readBody(conn, status);
        return new HttpResponse(status, body);
    }

    private static String readBody(HttpURLConnection conn, int status) throws IOException {
        InputStream is = (status >= 200 && status < 400)
                ? conn.getInputStream()
                : conn.getErrorStream();
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static class HttpResponse {
        final int statusCode;
        final String body;
        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body       = body;
        }
    }


}




