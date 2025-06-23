package org.example.user.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionCookieUtil {

    /**
     * 사용자 ID와 사용자명으로 세션을 생성하고,
     * HttpOnly 쿠키를 응답 헤더에 추가한다.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param userId   로그인한 사용자 ID
     * @param username 로그인한 사용자 이름
     */
    public static void createSessionAndCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            Long userId,
            String username
    ) {
        HttpSession session = request.getSession(true);
        session.setAttribute("loginUserId", userId);
        session.setAttribute("loginUsername", username);

        Cookie jsess = new Cookie("JSESSIONID", session.getId());
        jsess.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        jsess.setHttpOnly(true);
        response.addCookie(jsess);
    }
}