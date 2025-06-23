package org.example.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.friend.dao.FriendDAO;
import org.example.friend.dto.*;
import org.example.friend.service.FriendService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * FriendController: "/api/friends/*" 로 매핑되어,
 * - POST   /api/friends          → 친구 추가 (JSON body: { "username":"...", "friendUsername":"..." })
 * - GET    /api/friends?username=xxx → 친구 목록 조회
 * - DELETE /api/friends          → 친구 삭제 (JSON body: { "username":"...", "friendUsername":"..." })
 */
public class FriendController extends HttpServlet {

    private FriendService friendService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("FriendController.init 진입 성공");

        // 1) db.properties 파일 로딩
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/db.properties")) {
            if (is == null) {
                throw new ServletException("db.properties 파일을 찾을 수 없습니다.");
            }
            props.load(is);
        } catch (IOException e) {
            throw new ServletException("db.properties 로딩 실패", e);
        }

        System.out.println("▶ FriendController에서 읽은 jdbc.url = " + props.getProperty("jdbc.url"));
        System.out.println("▶ FriendController에서 읽은 jdbc.username = " + props.getProperty("jdbc.username"));

        HikariDataSource ds;
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("jdbc.url"));
            config.setUsername(props.getProperty("jdbc.username"));
            config.setPassword(props.getProperty("jdbc.password"));
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ServletException("HikariDataSource 초기화 중 예외 발생", e);
        }

        System.out.println(">>> 예외문 통과");

        FriendDAO friendDAO = new FriendDAO(ds);
        this.friendService = new FriendService(friendDAO);
        this.objectMapper = new ObjectMapper();

        System.out.println(">>> FriendController.init() 호출됨");
    }

    /**
     * 친구 목록 조회: GET /api/friends?username={username}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println(">>> FriendController.doGet() 진입: pathInfo=" + req.getPathInfo());

        // pathInfo가 null 또는 "/" 일 때만 처리
        String path = req.getPathInfo(); // /api/friends 로 요청하면 pathInfo == null
        if (path == null || "/".equals(path)) {
            String username = req.getParameter("username");
            if (username == null || username.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("MISSING_FIELD", "username 파라미터가 필요합니다.");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            GetFriendsListResDto resDto;
            GetFriendsListReqDto reqDto = new GetFriendsListReqDto(username);
            try {
                resDto = friendService.getFriendsList(reqDto);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("SERVER_ERROR", e.getMessage());
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), resDto);
            return;
        }

        // 그 외의 pathInfo는 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json; charset=UTF-8");
        ErrorResponse err = new ErrorResponse("Not Found", "지원하지 않는 GET 경로입니다: " + path);
        objectMapper.writeValue(resp.getWriter(), err);
    }

    /**
     * 친구 추가: POST /api/friends
     * JSON 바디 형식: { "username": "...", "friendUsername": "..." }
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println(">>> FriendController.doPost() 진입: pathInfo=" + req.getPathInfo());

        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            // 1) Content-Type 검사
            String contentType = req.getContentType();
            if (contentType == null || !contentType.startsWith("application/json")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("INVALID_CONTENT_TYPE", "Content-Type must be application/json");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 2) JSON 바디 파싱
            AddFriendReqDto requestDto;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"))) {
                requestDto = objectMapper.readValue(reader, AddFriendReqDto.class);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("INVALID_JSON", "JSON 형식이 잘못되었습니다.");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 3) 필수 필드 검사
            if (requestDto.getUsername() == null || requestDto.getFriendUsername() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("MISSING_FIELD", "username 및 friendUsername 필드가 필요합니다.");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 4) 친구 추가 서비스 호출
            AddFriendResDto responseDto;
            try {
                responseDto = friendService.addFriend(requestDto);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("SERVER_ERROR", e.getMessage());
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 5) 성공 응답
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), responseDto);
            return;
        }

        // 그 외의 pathInfo는 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json; charset=UTF-8");
        ErrorResponse err = new ErrorResponse("Not Found", "지원하지 않는 POST 경로입니다: " + path);
        objectMapper.writeValue(resp.getWriter(), err);
    }

    /**
     * 친구 삭제: DELETE /api/friends
     * JSON 바디 형식: { "username": "...", "friendUsername": "..." }
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println(">>> FriendController.doDelete() 진입: pathInfo=" + req.getPathInfo());

        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            // 1) Content-Type 검사
            String contentType = req.getContentType();
            if (contentType == null || !contentType.startsWith("application/json")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("INVALID_CONTENT_TYPE", "Content-Type must be application/json");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 2) JSON 바디 파싱
            RemoveFriendReqDto requestDto;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"))) {
                requestDto = objectMapper.readValue(reader, RemoveFriendReqDto.class);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("INVALID_JSON", "JSON 형식이 잘못되었습니다.");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 3) 필수 필드 검사
            if (requestDto.getUsername() == null || requestDto.getFriendUsername() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("MISSING_FIELD", "username 및 friendUsername 필드가 필요합니다.");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 4) 친구 삭제 서비스 호출
            RemoveFriendResDto responseDto;
            try {
                responseDto = friendService.removeFriend(requestDto);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("SERVER_ERROR", e.getMessage());
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 5) 성공 응답
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), responseDto);
            return;
        }

        // 그 외의 pathInfo는 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json; charset=UTF-8");
        ErrorResponse err = new ErrorResponse("Not Found", "지원하지 않는 DELETE 경로입니다: " + path);
        objectMapper.writeValue(resp.getWriter(), err);
    }
}
