package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.TestApiConfig;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MemoController의 메모 저장 · 조회 기능 통합 테스트
 *
 * - 서버 주소: http://localhost:8080
 * - 서블릿 매핑: context.addServlet(MemoController.class, "/api/memo/*");
 * - API 경로:
 *     • 메모 저장 : POST /api/memo/save
 *         JSON body: { "ownerUsername", "friendUsername", "createdDate", "content" }
 *     • 메모 조회 : GET  /api/memo/get?owner={ownerUsername}&friend={friendUsername}&date={yyyy-MM-dd}
 *
 * 이 테스트는 회원가입이나 로그인 과정을 생략하며,
 * 실제 데이터베이스에 해당 owner와 friend 계정이 이미 등록되어 있다고 가정합니다.
 * 반드시 테스트 실행 전에 EmbeddedServer(Jetty 등)가 기동 중이어야 합니다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoControllerTest {

    private static final String BASE_URL     = TestApiConfig.get("api.baseUrl");
    private static final String MEMO_SAVE    = TestApiConfig.get("api.memo.save");
    private static final String MEMO_GET     = TestApiConfig.get("api.memo.get");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 이미 DB에 존재한다고 가정할 사용자 정보
    private static final String OWNER= "testUser_1";
    private static final String FRIEND= "testUser_2";

    // 메모 테스트 데이터
    private static final String TEST_DATE    = "2025-06-08";
    private static final String TEST_CONTENT = "테스트 메모내용3";
    private static final String TEST_DATE_NONE = "2025-06-23";

    // 전역 CookieManager: 세션 쿠키가 필요한 경우 유지
    private static CookieManager cookieManager;

    @BeforeAll
    static void setUpCookieManager() {
        // 로그인 과정을 테스트하지 않으므로 쿠키 매니저만 초기화
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    /** 1) 메모 저장 기능 테스트 */
    @Test
    @Order(1)
    void testSaveMemo() throws Exception {
        // 요청 JSON 구성
        Map<String, String> payload = Map.of(
                "ownerUsername",  OWNER,
                "friendUsername", FRIEND,
                "createdDate",    TEST_DATE,
                "content",        TEST_CONTENT
        );

        String url = BASE_URL + MEMO_SAVE;
        HttpResponse res = sendPost(url, objectMapper.writeValueAsString(payload));

        System.out.println("[testSaveMemo] Response Body: " + res.body);
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("message"));
        assertTrue(root.has("memoId"));
        assertTrue(root.has("createdDate"));

        assertEquals("메모 저장 성공", root.get("message").asText());
        assertTrue(root.get("memoId").asLong() > 0);
        assertEquals(TEST_DATE, root.get("createdDate").asText());
    }

    /** 2) 특정 날짜 메모 조회 기능 테스트 */
    @Test
    @Order(2)
    void testGetMemoByDate() throws Exception {
        // 사전 저장 (없으면 저장)
        Map<String, String> savePayload = Map.of(
                "ownerUsername",  OWNER,
                "friendUsername", FRIEND,
                "createdDate",    TEST_DATE,
                "content",        TEST_CONTENT
        );
        sendPost(BASE_URL + MEMO_SAVE, objectMapper.writeValueAsString(savePayload));

        // 조회 요청
        String url = String.format(
                "%s%s?owner=%s&friend=%s&date=%s",
                BASE_URL, MEMO_GET, OWNER, FRIEND, TEST_DATE
        );
        HttpResponse res = sendGet(url);

        System.out.println("[testGetMemoByDate] Response Body: " + res.body);
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("content"));
        assertTrue(root.has("createdDate"));

        assertEquals(TEST_CONTENT, root.get("content").asText());
        assertEquals(TEST_DATE, root.get("createdDate").asText());
    }
    /** 3) 특정 날짜 메모 조회 기능 테스트 (메모 없는 날짜) */
    @Test
    @Order(3)
    void testGetMemoByDate_None() throws Exception {
        // 조회 요청 (메모 없는 날짜)
        String url = String.format("%s%s?owner=%s&friend=%s&date=%s",
                BASE_URL, MEMO_GET, OWNER, FRIEND, TEST_DATE_NONE);
        HttpResponse res = sendGet(url);

        System.out.println("[testGetMemoByDate_None] Response: " + res.body);
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        // content, createdDate 모두 null이어야 함
        assertTrue(root.has("content"));
        assertTrue(root.has("createdDate"));
        assertTrue(root.get("content").isNull(),
                "메모 없는 날짜의 content는 null이어야 합니다.");
        assertTrue(root.get("createdDate").isNull(),
                "메모 없는 날짜의 createdDate는 null이어야 합니다.");
    }

    // -------------------------------------------------------------------
    // Helper Method: HTTP POST 요청 보내기
    // -------------------------------------------------------------------
    // Helper methods
    private static HttpResponse sendPost(String url, String body) throws IOException {
        return sendRequest(url, "POST", body);
    }
    private static HttpResponse sendGet(String url) throws IOException {
        return sendRequest(url, "GET", null);
    }
    private static HttpResponse sendRequest(String urlString, String method, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int status = conn.getResponseCode();
        InputStream is = (status < 400)? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return new HttpResponse(status, sb.toString());
    }
    private record HttpResponse(int statusCode, String body) {}
}
