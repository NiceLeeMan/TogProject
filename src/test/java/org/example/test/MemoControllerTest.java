package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

    private static final String BASE_URL   = "http://localhost:8080";
    private static final String MEMO_URL   = BASE_URL + "/api/memo";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 이미 DB에 존재한다고 가정할 사용자 정보
    private static final String TEST_OWNER_USERNAME  = "qwe12345";
    private static final String TEST_FRIEND_USERNAME = "qwe123456";

    // 메모 테스트 데이터
    private static final String TEST_DATE    = "2025-06-06";
    private static final String TEST_CONTENT = "";

    // 전역 CookieManager: 세션 쿠키가 필요한 경우 유지
    private static CookieManager cookieManager;

    @BeforeAll
    static void setUpCookieManager() {
        // 로그인 과정을 테스트하지 않으므로 쿠키 매니저만 초기화
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * 1) 메모 저장 기능 테스트
     *    POST /api/memo/save
     */
    @Test
    @Order(1)
    @DisplayName("메모 저장 기능: POST /api/memo/save")
    void testSaveMemo() throws IOException {
        String saveUrl = MEMO_URL + "/save";

        // 요청 JSON 구성
        String requestJson = String.format(
                "{\"ownerUsername\":\"%s\",\"friendUsername\":\"%s\",\"createdDate\":\"%s\",\"content\":\"%s\"}",
                TEST_OWNER_USERNAME, TEST_FRIEND_USERNAME, TEST_DATE, TEST_CONTENT
        );

        HttpResponse response = sendPostRequest(saveUrl, requestJson);

        // 상태 코드 200 확인
        assertEquals(200, response.statusCode,
                "메모 저장 시 HTTP 상태 코드는 200이어야 합니다. 실제: " + response.statusCode);

        // 응답 JSON 파싱 및 필드 검증
        JsonNode root = objectMapper.readTree(response.body);
        assertTrue(root.has("message"),     "응답에 'message' 필드가 포함되어야 합니다.");
        assertTrue(root.has("memoId"),      "응답에 'memoId' 필드가 포함되어야 합니다.");
        assertTrue(root.has("createdDate"), "응답에 'createdDate' 필드가 포함되어야 합니다.");

        String message = root.get("message").asText();
        long memoId    = root.get("memoId").asLong();
        String date    = root.get("createdDate").asText();

        assertEquals("메모 저장 성공", message,
                "메모 저장 성공 시 message 값은 \"메모 저장 성공\"이어야 합니다. 실제: " + message);
        assertTrue(memoId > 0,
                "memoId는 0보다 큰 값이어야 합니다. 실제: " + memoId);
        assertEquals(TEST_DATE, date,
                "서버가 반환한 createdDate가 요청한 날짜와 동일해야 합니다. 실제: " + date);
    }

    /**
     * 2) 특정 날짜의 메모 조회 기능 테스트
     *    GET /api/memo/get?owner={ownerUsername}&friend={friendUsername}&date={yyyy-MM-dd}
     */
    @Test
    @Order(2)
    @DisplayName("메모 조회 기능: GET /api/memo/get?owner=&friend=&date=")
    void testGetMemoByDate() throws IOException {
        // --- 사전 준비: 같은 날짜에 메모 저장 (이미 저장되어 있지 않다면) ---
        String saveUrl = MEMO_URL + "/save";
        String saveJson = String.format(
                "{\"ownerUsername\":\"%s\",\"friendUsername\":\"%s\",\"createdDate\":\"%s\",\"content\":\"%s\"}",
                TEST_OWNER_USERNAME, TEST_FRIEND_USERNAME, TEST_DATE, TEST_CONTENT
        );
        HttpResponse saveRes = sendPostRequest(saveUrl, saveJson);
        assertEquals(200, saveRes.statusCode,
                "조회 전 사전 저장 시에도 HTTP 상태 코드는 200이어야 합니다. 실제: " + saveRes.statusCode);

        // --- 메모 조회 요청 ---
        String getUrl = String.format(
                MEMO_URL + "/get?owner=%s&friend=%s&date=%s",
                TEST_OWNER_USERNAME, TEST_FRIEND_USERNAME, TEST_DATE
        );
        HttpResponse getRes = sendGetRequest(getUrl);

        // 상태 코드 200 확인
        assertEquals(200, getRes.statusCode,
                "메모 조회 시 HTTP 상태 코드는 200이어야 합니다. 실제: " + getRes.statusCode);

        // 응답 JSON 파싱 및 필드 검증
        JsonNode root = objectMapper.readTree(getRes.body);
        assertTrue(root.has("content"),     "응답에 'content' 필드가 포함되어야 합니다.");
        assertTrue(root.has("createdDate"), "응답에 'createdDate' 필드가 포함되어야 합니다.");

        String content   = root.get("content").asText();
        String createdOn = root.get("createdDate").asText();

        System.out.println("조회된 메모 내용: " + content);
        System.out.println("조회된 메모 날짜: " + createdOn);

        assertEquals(TEST_CONTENT, content,
                "조회된 content가 저장된 내용과 일치해야 합니다. 실제: " + content);
        assertEquals(TEST_DATE, createdOn,
                "조회된 createdDate가 요청한 날짜와 동일해야 합니다. 실제: " + createdOn);
    }

    // -------------------------------------------------------------------
    // Helper Method: HTTP POST 요청 보내기
    // -------------------------------------------------------------------
    private static HttpResponse sendPostRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int statusCode = conn.getResponseCode();
        InputStream is = (statusCode >= 200 && statusCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder responseBuilder = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
            }
        }

        return new HttpResponse(statusCode, responseBuilder.toString());
    }

    // -------------------------------------------------------------------
    // Helper Method: HTTP GET 요청 보내기
    // -------------------------------------------------------------------
    private static HttpResponse sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int statusCode = conn.getResponseCode();
        InputStream is = (statusCode >= 200 && statusCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder responseBuilder = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line.trim());
                }
            }
        }

        return new HttpResponse(statusCode, responseBuilder.toString());
    }

    /**
     * 상태 코드와 응답 바디를 함께 담아 반환하는 DTO
     */
    private static class HttpResponse {
        final int statusCode;
        final String body;

        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body       = body;
        }
    }
}
