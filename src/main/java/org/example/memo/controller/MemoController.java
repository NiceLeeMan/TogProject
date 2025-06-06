package org.example.memo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.memo.dao.MemoDAO;
import org.example.memo.dto.GetMemoReq;
import org.example.memo.dto.GetMemoRes;
import org.example.memo.dto.PostMemoReq;
import org.example.memo.dto.PostMemoRes;
import org.example.memo.service.MemoService;
import org.example.user.dao.UserDAO;


import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * MemoController: DTO 기반으로 요청을 받고 MemoService의 메서드를 호출합니다.
 *
 * 1) init()에서 DataSource, DAO, Service, ObjectMapper를 초기화합니다.
 * 2) doGet, doPost, doDelete에서 pathInfo를 기준으로 핸들러를 호출합니다.
 *
 *  - GET    /memo/get?owner={ownerUsername}&friend={friendUsername}&date={yyyy-MM-dd}
 *             → 특정 날짜 메모 조회 (GetMemoReq → GetMemoRes)
 *  - POST   /memo/save
 *             Body(JSON): PostMemoReq(ownerUsername, friendUsername, createdDate, content)
 *             → 메모 생성/수정 (PostMemoReq → PostMemoRes)
 *  - DELETE /memo/delete?owner={ownerUsername}&friend={friendUsername}&date={yyyy-MM-dd}
 *             → 특정 날짜 메모 삭제 (GetMemoReq → boolean)
 */
public class MemoController extends HttpServlet {
    private MemoService memoService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println(">>> MemoController.init() 호출됨");

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
        DataSource ds = new HikariDataSource(config);

        // 3) DAO, Service, Jackson ObjectMapper 초기화
        UserDAO userDAO = new UserDAO(ds);
        MemoDAO memoDAO = new MemoDAO(ds);

        this.memoService = new MemoService(memoDAO, userDAO);
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println(">>> MemoController.doGet() 진입: pathInfo=" + request.getPathInfo());
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo(); // expected "/get"
        try (PrintWriter out = response.getWriter()) {
            if ("/get".equals(path)) {
                handleGetMemo(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"지원하지 않는 GET 경로입니다.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println(">>> MemoController.doPost() 진입: pathInfo=" + request.getPathInfo());
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo(); // expected "/save"
        try (PrintWriter out = response.getWriter()) {
            if ("/save".equals(path)) {
                handleSaveMemo(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"지원하지 않는 POST 경로입니다.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /memo/get?owner={ownerUsername}&friend={friendUsername}&date={yyyy-MM-dd}
     */
    private void handleGetMemo(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        String ownerUsername  = request.getParameter("owner");
        String friendUsername = request.getParameter("friend");
        String dateStr        = request.getParameter("date");

        if (ownerUsername == null || friendUsername == null || dateStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"owner, friend, date 파라미터가 모두 필요합니다.\"}");
            return;
        }

        GetMemoReq reqDto = new GetMemoReq();
        reqDto.setOwnerUsername(ownerUsername);
        reqDto.setFriendUsername(friendUsername);
        reqDto.setCreatedDate(java.time.LocalDate.parse(dateStr));

        GetMemoRes resDto = null;
        try {
            resDto = memoService.getMemo(reqDto);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
            return;
        }

        if (resDto == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\":\"해당 사용자 정보가 없거나 메모 조회 중 오류 발생.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write(objectMapper.writeValueAsString(resDto));
        }
    }

    /**
     * POST /memo/save
     * Body(JSON): PostMemoReq(ownerUsername, friendUsername, createdDate, content)
     */
    private void handleSaveMemo(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        PostMemoReq reqDto = objectMapper.readValue(request.getInputStream(), PostMemoReq.class);

        if (reqDto.getOwnerUsername() == null
                || reqDto.getFriendUsername() == null
                || reqDto.getCreatedDate() == null
                || reqDto.getContent() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"ownerUsername, friendUsername, createdDate, content는 필수입니다.\"}");
            return;
        }


        PostMemoRes resDto;
        try {
            resDto = memoService.saveOrUpdateMemo(reqDto);
        } catch (IllegalArgumentException iae) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"" + iae.getMessage() + "\"}");
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.write(objectMapper.writeValueAsString(resDto));
    }


}
