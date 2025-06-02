package org.example.user.controller;




import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.user.dao.UserDAO;
import org.example.user.dto.SignInReqDto;
import org.example.user.dto.SignInResDto;
import org.example.user.dto.SignUpReqDto;
import org.example.user.dto.SignUpResDto;
import org.example.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * UserController: HTTP 요청을 처리하여 Service를 호출하고, JSON 응답을 반환하는 서블릿
 *
 * - /api/user/signup  : POST { "name": "...", "userId": "...", "password": "..." }
 * - /api/user/signin  : POST { "userId": "...", "password": "..." }
 * - /api/user/signout : POST { "userId": "..." }
 *
 * Jackson ObjectMapper를 사용해 JSON 직렬화/역직렬화 처리
 */

@WebServlet(name = "UserController", urlPatterns = {"/api/user/*"})
public class UserController extends HttpServlet {

    private UserService userService;
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
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(props.getProperty("jdbc.url"));
        config.setUsername(props.getProperty("jdbc.username"));
        config.setPassword(props.getProperty("jdbc.password"));
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource(config);

        // 3) DAO, Service, Jackson ObjectMapper 초기화
        UserDAO userDAO = new UserDAO(ds);
        this.userService = new UserService(userDAO);
        this.objectMapper = new ObjectMapper();
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 요청 경로 추출: /signup, /signin, /signout
        String path = request.getPathInfo();
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        switch (path) {
            case "/signup":
                handleSignUp(request, response);
                break;
            case "/signin":
                handleSignIn(request, response);
                break;
            case "/signout":
                handleSignOut(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try (PrintWriter out = response.getWriter()) {
                    out.write("{\"error\":\"지원하지 않는 경로입니다.\"}");
                }
        }
    }

    /**
     * 회원가입 처리 핸들러
     * 요청 JSON → SignUpReqDto로 변환 → Service.signUp 호출 → SignUpResDto를 JSON으로 응답
     */
    private void handleSignUp(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SignUpReqDto reqDto = objectMapper.readValue(request.getInputStream(), SignUpReqDto.class);
        SignUpResDto resDto = userService.signUp(reqDto);

        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.write(objectMapper.writeValueAsString(resDto));
        }
    }

    /**
     * 로그인 처리 핸들러
     * 요청 JSON → SignInReqDto로 변환 → Service.signIn 호출 → SignInResDto(JSON) 또는 401 응답
     */
    private void handleSignIn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SignInReqDto reqDto = objectMapper.readValue(request.getInputStream(), SignInReqDto.class);
        SignInResDto resDto = userService.signIn(reqDto);

        if (resDto == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
            }
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.write(objectMapper.writeValueAsString(resDto));
        }
    }

    /**
     * 로그아웃 처리 핸들러
     * 요청 JSON: { "userId": "hong123" }
     * Service.signOut 호출 → 성공 여부에 따라 200 또는 400 응답
     */
    private void handleSignOut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> body = objectMapper.readValue(
                request.getInputStream(),
                java.util.Map.class
        );
        String userId = body.get("userId");

        if (userId == null || userId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"userId가 필요합니다.\"}");
            }
            return;
        }

        boolean ok = userService.signOut(userId);
        if (!ok) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"로그아웃에 실패했습니다.(사용자 없음 또는 내부 오류)\"}");
            }
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.write("{\"message\":\"로그아웃 성공\"}");
        }
    }
}
