package org.example.user.session.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.config.DataSoruceConfig;
import org.example.user.common.dao.UserDAO;

import org.example.user.session.dto.SignOutResDto;
import org.example.user.session.service.SignOutService;


import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/signout")
public class SignOutController extends HttpServlet {

    private SignOutService SignOutService;
    private ObjectMapper objectMapper;


    @Override
    public void init() throws ServletException {
        super.init();
        HikariDataSource ds       = DataSoruceConfig.getDataSource();
        UserDAO userDAO           = new UserDAO(ds);
        this.SignOutService       = new SignOutService(userDAO);
        this.objectMapper         = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUserId") == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                SignOutResDto dto = SignOutResDto.ofFailure(
                        /* ErrorCode.SERVER_ERROR */ null,
                        "로그인된 세션이 없습니다."
                );
                out.write(objectMapper.writeValueAsString(dto));
            }
            return;
        }

        Long userId = (Long) session.getAttribute("loginUserId");
        boolean ok  = SignOutService.signOut(userId);

        session.invalidate();
        response.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try (PrintWriter out = response.getWriter()) {
            SignOutResDto dto = ok
                    ? SignOutResDto.ofSuccess("로그아웃 되었습니다.")
                    : SignOutResDto.ofFailure(/* ErrorCode.SERVER_ERROR */ null, "로그아웃에 실패했습니다.");
            out.write(objectMapper.writeValueAsString(dto));
        }
    }
}
