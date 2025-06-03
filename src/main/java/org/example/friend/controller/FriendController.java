package org.example.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.friend.dto.*;
import org.example.friend.service.FriendService;
import org.example.friend.dao.FriendDAO;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/**
 * FriendController: "/api/friends/*" 로 매핑되어,
 * - POST   /api/friends       → 친구 추가
 * - GET    /api/friends?username=xxx  → 친구 목록 조회
 * - DELETE /api/friends       → 친구 삭제 (body: username, friendUsername)
 */
public class FriendController extends HttpServlet {

    private FriendService friendService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        // DataSource 설정(예: HikariCP) 부분
        DataSource dataSource = (DataSource) getServletContext().getAttribute("datasource");
        // 만약 getServletContext()에 datasource가 등록되어 있지 않으면, 직접 생성해도 됩니다:
        // HikariConfig config = new HikariConfig();
        // config.setJdbcUrl(...); config.setUsername(...); config.setPassword(...);
        // DataSource dataSource = new HikariDataSource(config);
        this.friendService = new FriendService(new FriendDAO(dataSource));
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

        // pathInfo는 "/something" 또는 "/" 또는 null. 우리는 GET /api/friends?username=xxx 만 처리
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            String username = req.getParameter("username");
            if (username == null || username.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json; charset=UTF-8");
                ErrorResponse err = new ErrorResponse("MISSING_FIELD", "username 파라미터가 필요합니다.");
                objectMapper.writeValue(resp.getWriter(), err);
                return;
            }

            // 서비스 호출
            GetFriendsListResDto resDto;
            GetFriendsListReqDto reqDto = new GetFriendsListReqDto();
            resDto = friendService.getFriendsList(reqDto);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), resDto);
            return;
        }

        // 그 외 pathInfo는 처리하지 않음 → 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json; charset=UTF-8");
        ErrorResponse err = new ErrorResponse("Not Found", "지원하지 않는 GET 경로입니다: " + path);
        objectMapper.writeValue(resp.getWriter(), err);
    }

    /**
     * 친구 추가: POST /api/friends (body: JSON { "username":"hong123", "friendUsername":"kim456" })
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println(">>> FriendController.doPost() 진입: pathInfo=" + req.getPathInfo());

        // pathInfo가 null 또는 "/" 일 때만 친구 추가
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

            // 4) 친구 추가 로직 호출
            AddFriendResDto responseDto;
            responseDto = friendService.addFriend(requestDto);

            // 5) 성공 응답
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), responseDto);
            return;
        }

        // pathInfo가 그 외면 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json; charset=UTF-8");
        ErrorResponse err = new ErrorResponse("Not Found", "지원하지 않는 POST 경로입니다: " + path);
        objectMapper.writeValue(resp.getWriter(), err);
    }

    /**
     * 친구 삭제: DELETE /api/friends (body: JSON { "username":"hong123", "friendUsername":"kim456" })
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

            // 2) JSON 파싱
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

            // 4) 친구 삭제 로직 호출
            RemoveFriendResDto responseDto;
            responseDto = friendService.removeFriend(requestDto);

            // 5) 성공 응답
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json; charset=UTF-8");
            objectMapper.writeValue(resp.getWriter(), responseDto);
            return;
        }

        // pathInfo 그 외는 404
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json; charset=UTF-8");
        ErrorResponse err = new ErrorResponse("Not Found", "지원하지 않는 DELETE 경로입니다: " + path);
        objectMapper.writeValue(resp.getWriter(), err);
    }
}
