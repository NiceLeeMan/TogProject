package org.example.user.authentication.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.config.DataSoruceConfig;
import org.example.user.authentication.dto.SignInResDto;
import org.example.user.authentication.service.Authenticator;
import org.example.user.authentication.service.SessionManager;
import org.example.user.common.dao.UserDAO;
import org.example.user.authentication.dto.SignInReqDto;
import org.example.user.authentication.service.SignInService;
import org.example.user.common.utils.SessionCookieUtil;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/signin")
public class SignInController extends HttpServlet {

    private SignInService signInService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        HikariDataSource ds    = DataSoruceConfig.getDataSource();
        UserDAO userDAO        = new UserDAO(ds);
        Authenticator authenticator     = new Authenticator(userDAO);
        SessionManager sessionManager   = new SessionManager(userDAO);

        this.signInService = new SignInService(authenticator, sessionManager);
        this.objectMapper      = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        // 1) 요청 바디 → DTO
        SignInReqDto reqDto = objectMapper.readValue(request.getInputStream(), SignInReqDto.class);
        // 2) 비즈니스 호출
        SignInResDto resDto = signInService.signIn(reqDto);

        // 3) 인증 실패
        if (resDto == null || !resDto.isSuccess()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
            }
            return;
        }

        // 4) 세션 및 쿠키 설정
        SessionCookieUtil.createSessionAndCookie(
                request, response,
                resDto.getUserInfo().getId(),
                resDto.getUserInfo().getUsername()
        );

        // 5) 응답
        System.out.println(reqDto);
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter out = response.getWriter()) {
            out.write(objectMapper.writeValueAsString(resDto));
        }
    }
}
