package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.TestApiConfig;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FriendController의 친구 추가 · 삭제 · 친구 목록 조회 기능 통합 테스트
 *
 * - 서버 주소: http://localhost:8080
 * - API 경로:
 *     • 친구 추가    : POST   /api/friends
 *     • 친구 삭제    : DELETE /api/friends
 *     • 친구 목록 조회: GET    /api/friends?username={username}
 *
 * 반드시 테스트를 실행하기 전에 Jetty 서버(EmbeddedServer)가 기동 중이어야 합니다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FriendControllerTest {

    private static final String BASE_URL = TestApiConfig.get("api.baseUrl");
    private static final String FRIENDS_PATH = TestApiConfig.get("api.friends.add");
    private static final String USER_SIGNUP = TestApiConfig.get("api.user.signup");
    private static final String USER_SIGNIN = TestApiConfig.get("api.user.signin");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // DB에 이미 존재하는 테스트용 사용자 정보 (이미 SignUp되어 있음)
    private static final long testUser1Id = 19L;
    private static final String testUser1 = "testUser_1";
    private static final String testPass1 = "pw@1001";

    private static final long testUser2Id = 21L;
    private static final String testUser2 = "testUser_3";


    // 전역 CookieManager: 로그인 세션 쿠키(JSESSIONID 등)를 유지하도록 함
    private static java.net.CookieManager cookieManager;

    @BeforeAll
    static void beforeAll() {
        // CookieManager 설정
        java.net.CookieManager cm = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(cm);
    }

    @Test
    @Order(1)
    void testAddFriend() throws Exception {
        // testUser1 로그인 (세션 쿠키 취득)
        Map<String, String> login = Map.of(
                "username", testUser1,
                "password", testPass1
        );
        sendPost(BASE_URL + USER_SIGNIN, objectMapper.writeValueAsString(login));

        // 친구 추가 요청 (testUser1이 testUser2를 친구로 추가)
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", testUser1);
        payload.put("friendUsername", testUser2);

        HttpResponse res = sendPost(
                BASE_URL + FRIENDS_PATH,
                objectMapper.writeValueAsString(payload)
        );
        assertEquals(200, res.statusCode, "POST 실패");

        JsonNode root = objectMapper.readTree(res.body);
        assertEquals("OK", root.get("statusCode").asText());

        boolean found = false;
        for (JsonNode f : root.get("friendsList")) {
            if (testUser2.equals(f.get("username").asText()) &&
                    f.get("userId").asLong() == testUser2Id) {
                found = true;
                break;
            }
        }
        assertTrue(found, "DB에 있는 testUser_2 (ID=20)가 목록에 있어야 합니다.");
    }

    /**
     * 2. 친구 목록 조회 테스트
     */
    @Test
    @Order(2)
    void testGetFriendsList() throws Exception {
        String url = String.format(
                "%s%s?username=%s",
                BASE_URL, FRIENDS_PATH, testUser1
        );
        HttpResponse res = sendGet(url);
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("friendsList"), "friendsList 필드가 존재해야 합니다.");

        List<String> usernames = new ArrayList<>();
        for (JsonNode f : root.get("friendsList")) {
            usernames.add(f.get("username").asText());
        }
        System.out.println("나의 친구 목록: " + usernames);

        // 최소 한 명 이상 친구가 있어야 하고, testUser2가 포함되어야 함
        assertFalse(usernames.isEmpty(), "친구 목록이 비어 있으면 안됩니다.");


    }


    /**
     * 3. 친구 삭제 테스트
     */
    /**
     * 3. 친구 삭제 테스트
     */
    @Test
    @Order(3)
    void testRemoveFriend() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", testUser1);
        payload.put("friendUsername", testUser2);

        HttpResponse res = sendDelete(
                BASE_URL + FRIENDS_PATH,
                objectMapper.writeValueAsString(payload)
        );
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.get("message").asText().contains("삭제 성공"));

        List<Long> ids = new ArrayList<>();
        for (JsonNode f : root.get("friendsList")) {
            ids.add(f.get("userId").asLong());
        }
        assertFalse(ids.contains(testUser2Id),
                "삭제된 친구 ID=20이 목록에 있으면 안됩니다.");
    }

    // -------------------------------------------------------------------
    //  Helper Method: HTTP POST 요청 보내기
    // -------------------------------------------------------------------
    private static HttpResponse sendPostRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        // JSON 바디를 주는 경우
        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 쿠키 유지: 전역 CookieManager가 설정되어 있어 자동으로 쿠키가 관리됩니다.
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        // 응답 바디 읽기
        StringBuilder responseText = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseText.append(line);
                }
            }
        }

        return new HttpResponse(status, responseText.toString());
    }

    // -------------------------------------------------------------------
    //  Helper Method: HTTP DELETE 요청 보내기
    // -------------------------------------------------------------------
    // Helper methods
    private static HttpResponse sendPost(String url, String body) throws IOException {
        return sendRequest(url, "POST", body);
    }

    private static HttpResponse sendDelete(String url, String body) throws IOException {
        return sendRequest(url, "DELETE", body);
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
        InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder resp = new StringBuilder();
        if (is != null)
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) resp.append(line);
            }
        return new HttpResponse(status, resp.toString());
    }

    private static class HttpResponse {
        int statusCode;
        String body;

        HttpResponse(int s, String b) {
            statusCode = s;
            body = b;
        }
    }
}