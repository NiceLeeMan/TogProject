package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String BASE_URL_FRIEND = "http://localhost:8080/api/friends";
    private static final String BASE_URL_USER   = "http://localhost:8080/api/user";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 테스트용 사용자 정보 (각각 유니크하게 생성)
    private static final String testUser1     = "qwe12345";
    private static final String testPass1     = "qwe@12345";
    private static final String testName1     = "고종화";

    private static final String testUser2     = "qwe123457";
    private static final String testPass2     = "qwe@123457";
    private static final String testName2     = "정태우";

    // 전역 CookieManager: 로그인 세션 쿠키(JSESSIONID 등)를 유지하도록 함
    private static java.net.CookieManager cookieManager;

    @BeforeAll
    static void setUp() throws Exception {
        // CookieManager 설정
        cookieManager = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(cookieManager);

        // 1) testUser1 회원가입 (이미 존재하면 409이 오더라도 무시)
        Map<String, String> signupPayload1 = new HashMap<>();
        signupPayload1.put("username", testUser1);
        signupPayload1.put("password", testPass1);
        signupPayload1.put("name",     testName1);

        HttpResponse res1 = sendPostRequest(BASE_URL_USER + "/signup",
                objectMapper.writeValueAsString(signupPayload1));
        // 201 Created OR 409 Conflict(이미 존재) 모두 무시하고 진행

        // 2) testUser2 회원가입
        Map<String, String> signupPayload2 = new HashMap<>();
        signupPayload2.put("username", testUser2);
        signupPayload2.put("password", testPass2);
        signupPayload2.put("name",     testName2);

        HttpResponse res2 = sendPostRequest(BASE_URL_USER + "/signup",
                objectMapper.writeValueAsString(signupPayload2));
        // 201 Created OR 409 Conflict 무시
    }

    /**
     * 1. 친구 추가 테스트
     */
    @Test
    @Order(1)
    void testAddFriend() throws Exception {
        // 1) 요청 JSON 구성 (testUser1이 testUser2를 친구로 추가)
        Map<String, String> payload = new HashMap<>();
        payload.put("username",       testUser1);
        payload.put("friendUsername", testUser2);

        // 2) HTTP POST 요청
        HttpResponse res = sendPostRequest(BASE_URL_FRIEND, objectMapper.writeValueAsString(payload));

        // 3) 상태 코드 확인 (200 OK)
        assertEquals(200, res.statusCode, "친구 추가 시 HTTP 상태 코드는 200이어야 합니다. 실제: " + res.statusCode);

        // 4) 응답 바디 JSON 파싱
        JsonNode root = objectMapper.readTree(res.body);

        // 필수 필드: statusCode, friendsList
        assertTrue(root.has("statusCode"), "응답에 'statusCode' 필드가 있어야 합니다.");
        assertTrue(root.has("friendsList"), "응답에 'friendsList' 필드가 있어야 합니다.");

        String statusCode = root.get("statusCode").asText();
        assertEquals("OK", statusCode, "친구 추가 성공 시 statusCode는 \"OK\"여야 합니다. 실제: " + statusCode);

        JsonNode friendsArray = root.get("friendsList");
        assertTrue(friendsArray.isArray(), "'friendsList'는 배열이어야 합니다.");
        assertTrue(friendsArray.size() >= 1, "친구 목록에 최소 한 명 이상 있어야 합니다. 실제 크기: " + friendsArray.size());

        // 친구 목록 안에 testUser2가 포함되어 있는지 확인
        boolean found = false;
        for (JsonNode friendNode : friendsArray) {
            if (friendNode.has("username") && testUser2.equals(friendNode.get("username").asText())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "추가된 친구 '" + testUser2 + "'가 친구 목록에 포함되어야 합니다.");
    }

    /**
     * 2. 친구 목록 조회 테스트 (로그인 없이, username 파라미터만으로 조회)
     *     • 친구 목록을 콘솔에도 출력
     */
    @Test
    @Order(2)
    void testGetFriendsList() throws Exception {
        // 1) HTTP GET 요청: 친구 목록 조회 (쿼리 파라미터로 username 전달)
        String urlWithParam = BASE_URL_FRIEND + "?username=" + testUser1;
        HttpResponse getRes = sendGetRequest(urlWithParam);

        // 2) 상태 코드 확인 (200 OK)
        assertEquals(200, getRes.statusCode, "친구 목록 조회 시 HTTP 상태 코드는 200이어야 합니다. 실제: " + getRes.statusCode);

        // 3) 응답 바디 JSON 파싱
        JsonNode root = objectMapper.readTree(getRes.body);

        // 필수 필드: friendsList
        assertTrue(root.has("friendsList"), "응답에 'friendsList' 필드가 있어야 합니다.");

        JsonNode friendsArray = root.get("friendsList");
        assertTrue(friendsArray.isArray(), "'friendsList'는 배열이어야 합니다.");

        // 콘솔에 "나에게 등록된 친구" 목록 출력
        List<String> friendUsernames = new ArrayList<>();
        for (JsonNode friendNode : friendsArray) {
            if (friendNode.has("username")) {
                friendUsernames.add(friendNode.get("username").asText());
            }
        }
        System.out.println("나에게 등록된 친구: " + friendUsernames);


    }


    /**
     * 3. 친구 삭제 테스트
     */
    @Test
    @Order(3)
    void testRemoveFriend() throws Exception {
        // 1) 요청 JSON 구성 (testUser1이 testUser2를 친구에서 삭제)
        Map<String, String> payload = new HashMap<>();
        payload.put("username",       testUser1);
        payload.put("friendUsername", testUser2);

        // 2) HTTP DELETE 요청
        HttpResponse res = sendDeleteRequest(BASE_URL_FRIEND, objectMapper.writeValueAsString(payload));

        // 3) 상태 코드 확인 (200 OK)
        assertEquals(200, res.statusCode, "친구 삭제 시 HTTP 상태 코드는 200이어야 합니다. 실제: " + res.statusCode);

        // 4) 응답 바디 JSON 파싱
        JsonNode root = objectMapper.readTree(res.body);

        // 필수 필드: message, friendsList
        assertTrue(root.has("message"),     "응답에 'message' 필드가 있어야 합니다.");
        assertTrue(root.has("friendsList"), "응답에 'friendsList' 필드가 있어야 합니다.");

        String message = root.get("message").asText();
        assertTrue(message.contains("삭제 성공"), "친구 삭제 성공 시 메시지에 '삭제 성공'이 포함되어야 합니다. 실제: " + message);

        JsonNode friendsArray = root.get("friendsList");
        assertTrue(friendsArray.isArray(), "'friendsList'는 배열이어야 합니다.");

        // 삭제 후 리스트에 여전히 testUser2가 남아 있지 않은지 확인
        List<String> remainingUsernames = new ArrayList<>();
        for (JsonNode friendNode : friendsArray) {
            if (friendNode.has("username")) {
                remainingUsernames.add(friendNode.get("username").asText());
            }
        }
        assertFalse(remainingUsernames.contains(testUser2),
                "삭제된 친구 '" + testUser2 + "'가 여전히 친구 목록에 포함되어 있으면 안 됩니다. 실제 목록: " + remainingUsernames);
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
    private static HttpResponse sendDeleteRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

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
    //  Helper Method: HTTP GET 요청 보내기
    // -------------------------------------------------------------------
    private static HttpResponse sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

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

    /**
     * 상태 코드와 응답 바디를 함께 담아 반환하는 단순 DTO
     */
    private static class HttpResponse {
        int statusCode;
        String body;

        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
