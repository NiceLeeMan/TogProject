package org.example.friend.controller;

// ────────────────────────────────────────────────────────────────────────────────
// File: FriendController.java
// Path: src/main/java/org/example/friend/controller/FriendController.java
// ────────────────────────────────────────────────────────────────────────────────


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.friend.dto.*;
import org.example.friend.service.FriendService;


import java.io.BufferedReader;
import java.io.IOException;





/**
 * FriendController (Servlet)
 *
 * • 클라이언트(예: 데스크톱 앱 혹은 SPA)로부터 HTTP 요청을 받으면,
 *   JSON 바디나 쿼리 파라미터를 파싱하여 DTO로 변환한 뒤 Service를 호출하고,
 *   Service가 반환한 응답 DTO를 JSON으로 직렬화하여 클라이언트에 돌려준다.
 *
 * • 엔드포인트:
 *   GET    /friends?username={username}        → 친구 목록 조회
 *   POST   /friends                            → 친구 추가
 *   DELETE /friends                            → 친구 삭제
 *
 * • 요청/응답 모두 application/json을 사용하며,
 *   Jackson ObjectMapper를 이용해 DTO ↔ JSON 매핑을 수행한다.
 */
@WebServlet(name = "FriendController", urlPatterns = {"/friends"})
public class FriendController extends HttpServlet {

    private final FriendService friendService;
    private final ObjectMapper objectMapper;

    public FriendController() {
        this.friendService = new FriendService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 친구 목록 조회
     * GET /friends?username={username}
     *
     * 요청:
     *   • query param: username (String)
     *
     * 응답:
     *   • 상태코드 200, Content-Type: application/json
     *   • 바디: GetFriendsListResDto JSON
     *
     * 에러:
     *   • username 파라미터가 없으면 400 Bad Request
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws  IOException {

        String username = req.getParameter("username");
        if (username == null || username.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("USERNAME_REQUIRED", "username 쿼리 파라미터가 필요합니다.");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        GetFriendsListReqDto requestDto = new GetFriendsListReqDto();
        requestDto.setUsername(username);

        GetFriendsListResDto responseDto = friendService.getFriendsList(requestDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(resp.getWriter(), responseDto);
    }

    /**
     * 친구 추가
     * POST /friends
     *
     * 요청:
     *   • Content-Type: application/json
     *   • 바디: { "username": "...", "friendUsername": "..." }
     *
     * 응답:
     *   • 상태코드 200, Content-Type: application/json
     *   • 바디: AddFriendResDto JSON
     *
     * 에러:
     *   • JSON 파싱 실패: 400 Bad Request
     *   • 필수 필드 누락: 400 Bad Request
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!"application/json".equals(req.getContentType())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("INVALID_CONTENT_TYPE", "Content-Type must be application/json");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        AddFriendReqDto requestDto;
        try (BufferedReader reader = req.getReader()) {
            requestDto = objectMapper.readValue(reader, AddFriendReqDto.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("INVALID_JSON", "JSON 형식이 잘못되었습니다.");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        if (requestDto.getUsername() == null || requestDto.getFriendUsername() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("MISSING_FIELD", "username 및 friendUsername 필드가 필요합니다.");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        AddFriendResDto responseDto = friendService.addFriend(requestDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(resp.getWriter(), responseDto);
    }

    /**
     * 친구 삭제
     * DELETE /friends
     *
     * 요청:
     *   • Content-Type: application/json
     *   • 바디: { "username": "...", "friendUsername": "..." }
     *
     * 응답:
     *   • 상태코드 200, Content-Type: application/json
     *   • 바디: RemoveFriendResDto JSON
     *
     * 에러:
     *   • JSON 파싱 실패: 400 Bad Request
     *   • 필수 필드 누락: 400 Bad Request
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!"application/json".equals(req.getContentType())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("INVALID_CONTENT_TYPE", "Content-Type must be application/json");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        RemoveFriendReqDto requestDto;
        try (BufferedReader reader = req.getReader()) {
            requestDto = objectMapper.readValue(reader, RemoveFriendReqDto.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("INVALID_JSON", "JSON 형식이 잘못되었습니다.");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        if (requestDto.getUsername() == null || requestDto.getFriendUsername() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            ErrorResponse err = new ErrorResponse("MISSING_FIELD", "username 및 friendUsername 필드가 필요합니다.");
            objectMapper.writeValue(resp.getWriter(), err);
            return;
        }

        RemoveFriendResDto responseDto = friendService.removeFriend(requestDto);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(resp.getWriter(), responseDto);
    }

    /**
     * 공통 에러 응답 구조
     */
    private static class ErrorResponse {
        private final String code;
        private final String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }
    }
}
