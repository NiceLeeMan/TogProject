package org.example.user.registration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.config.DataSoruceConfig;
import org.example.user.common.dao.UserDAO;
import org.example.user.registration.dto.SignUpReqDto;
import org.example.user.registration.dto.SignUpResDto;
import org.example.user.registration.service.SignUpService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/signup")
public class SignUpController extends HttpServlet {

    private SignUpService signUpService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        HikariDataSource ds   = DataSoruceConfig.getDataSource();
        UserDAO userDAO       = new UserDAO(ds);
        this.signUpService    = new SignUpService(userDAO);
        this.objectMapper     = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        // 1) 요청 바디 → DTO
        SignUpReqDto reqDto = objectMapper.readValue(request.getInputStream(), SignUpReqDto.class);
        // 2) 비즈니스 호출
        SignUpResDto resDto = signUpService.signUp(reqDto);

        // 3) 상태 코드 설정
        if (resDto.isSuccess()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            switch (resDto.getCode()) {
                case DUPLICATE:
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    break;
                case INVALID_INPUT:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        // 4) JSON 응답
        try (PrintWriter out = response.getWriter()) {
            out.write(objectMapper.writeValueAsString(resDto));
        }
    }
}
