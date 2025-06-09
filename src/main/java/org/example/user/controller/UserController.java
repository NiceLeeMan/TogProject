package org.example.user.controller;




import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.example.user.dao.UserDAO;
import org.example.user.dto.*;
import org.example.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

///**
// * UserController: HTTP 요청을 처리하여 Service를 호출하고, JSON 응답을 반환하는 서블릿
// *
// * - /api/user/signup  : POST { "name": "...", "userId": "...", "password": "..." }
// * - /api/user/signin  : POST { "userId": "...", "password": "..." }
// * - /api/user/signout : POST { "userId": "..." }
// *
// * Jackson ObjectMapper를 사용해 JSON 직렬화/역직렬화 처리
// */

public class UserController extends HttpServlet {

    private UserService userService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {

        super.init();
        System.out.println(">>> UserController.init() 호출됨");
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
        HikariDataSource ds = new HikariDataSource(config);

        // 3) DAO, Service, Jackson ObjectMapper 초기화
        UserDAO userDAO = new UserDAO(ds);
        this.userService = new UserService(userDAO);
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println(">>> UserController.doPost() 진입: pathInfo=" + request.getPathInfo());
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
     * 회원가입 핸들러 (상태 코드 + ResDto 방식)
     */
    private void handleSignUp(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // 1) 요청 JSON → SignUpReqDto
        SignUpReqDto reqDto = objectMapper.readValue(request.getInputStream(), SignUpReqDto.class);

        // 2) Service 호출
        SignUpResDto resDto = userService.signUp(reqDto);

        // 3) 상태 코드 분기
        if (!resDto.isSuccess()) {
            String msg = resDto.getMessage();
            if (msg.contains("이미 존재")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
            } else if (msg.contains("서버 오류")) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            }
        } else {
            response.setStatus(HttpServletResponse.SC_CREATED); // 201
        }

        // 4) JSON 응답
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

        System.out.println("reqDto=" + reqDto +"/"+ "resDto=" + resDto);
        if (resDto == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
            }
            return;
        }

        // 2) 응답 헤더 준비 (Content-Type 먼저)
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // 3) 세션 생성 및 쿠키 헤더 강제 추가
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("loginUserId",   resDto.getId());
        httpSession.setAttribute("loginUsername", resDto.getUsername());
        // 수동으로 Set-Cookie 헤더 달기

        Cookie jsess = new Cookie("JSESSIONID", httpSession.getId());
        jsess.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        jsess.setHttpOnly(true);
        response.addCookie(jsess);

        System.out.println("response=" + response);

        System.out.println("httpSession=" + httpSession);

        // (선택) DB status=true 로 변경
        userService.updateStatus(resDto.getId(), true);

        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.write(objectMapper.writeValueAsString(resDto));
        }
    }

    // ↳ UserController.java 에서 handleSignOut 부분만 발췌

    private void handleSignOut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("Cookie header on /signout → " + request.getHeader("Cookie"));
        // 1) 세션 가져오기 (세션이 없으면 null)

        HttpSession httpSession = request.getSession(false);

        System.out.println("httpSession=" + httpSession);
        if (httpSession == null || httpSession.getAttribute("loginUserId") == null) {
            // 이미 로그아웃된 상태이거나 세션이 없는 경우
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                SignOutResDto dto = new SignOutResDto("로그인된 세션이 없습니다.");
                objectMapper.writeValue(out, dto);
            }
            return;
        }

        // 2) (선택) DB status=false 로 변경
        Long userId = (Long) httpSession.getAttribute("loginUserId");
        userService.updateStatus(userId, false);

        // 3) 세션 무효화
        httpSession.invalidate();

        // 4) 성공 응답
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            SignOutResDto dto = new SignOutResDto("로그아웃 성공");
            objectMapper.writeValue(out, dto);
        }
    }

}
