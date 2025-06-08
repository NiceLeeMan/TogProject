package org.example.test;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserController의 회원가입 · 로그인 · 로그아웃 기능 통합 테스트
 *
 * - 서버 주소: http://localhost:8080
 * - API 경로: /api/user/signup, /api/user/signin, /api/user/signout
 *
 * 반드시 테스트를 실행하기 전에 Jetty 서버(EmbeddedServer)가 기동 중이어야 합니다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    private static final String BASE_URL = "http://localhost:8080/api/user";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 테스트 도중 생성한 사용자 정보 저장용
    private static String testUsername = "qwe12345678";
    private static String testPassword = "qwe@12345678";
    private static String testName     = "김준호";

    private static Long createdUserId;

    @BeforeAll
    static void setUp() {
        // 매 테스트마다 유니크하게 생성
        cookieManager = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(cookieManager);
    }

    /**
     * 1. 회원가입 테스트
     */
    @Test
    @Order(1)
    void testSignUp() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", testUsername);
        payload.put("password", testPassword);
        payload.put("name",     testName);

        HttpResponse res = sendPostRequest(BASE_URL + "/signup",
                objectMapper.writeValueAsString(payload));

        System.out.println("회원가입 상태 코드: " + res.statusCode);
        System.out.println("회원가입 응답 바디: " + res.body);

        JsonNode root = objectMapper.readTree(res.body);

        if (res.statusCode == 201) {
            // ───────────── 첫 번째 실행(또는 DB에 없어서 정상 성공) ─────────────
            assertTrue(root.has("success"), "응답에 'success' 필드가 포함되어야 합니다.");
            assertTrue(root.has("message"), "응답에 'message' 필드가 포함되어야 합니다.");
            boolean success = root.get("success").asBoolean();
            String message = root.get("message").asText();

            assertTrue(success, "회원가입 성공 시 success 플래그는 true여야 합니다.");
            assertTrue(message.contains("성공"), "회원가입 성공 메시지가 와야 합니다. 실제: " + message);
        }
        else if (res.statusCode == 409) {
            // ───────────── 두 번째 실행 이후(중복) ─────────────
            assertTrue(root.has("success"), "중복 시에도 응답에 'success' 필드가 포함되어야 합니다.");
            assertTrue(root.has("message"), "중복 시에도 응답에 'message' 필드가 포함되어야 합니다.");
            boolean success = root.get("success").asBoolean();
            String message = root.get("message").asText();

            assertFalse(success, "중복 회원가입 시 success 플래그는 false여야 합니다.");
            assertTrue(message.contains("이미 존재"), "중복 시 '이미 존재' 메시지가 와야 합니다. 실제: " + message);
        }
        else {
            // 둘 다 아닌 경우, 의도치 않은 실패
            fail("예상치 못한 HTTP 상태 코드입니다: " + res.statusCode + ", Body: " + res.body);
        }
    }

    /**
     * 2. 로그인 테스트
     */
    @Test
    @Order(2)
    void testSignIn() throws Exception {
        // 1) 요청 JSON 구성 (이미 signup에서 만든 사용자로 로그인)
        Map<String, String> payload = new HashMap<>();
        payload.put("username", testUsername);
        payload.put("password", testPassword);

        // 2) HTTP POST 요청
        HttpResponse res = sendPostRequest(BASE_URL + "/signin", objectMapper.writeValueAsString(payload));

        // 3) 상태 코드 확인 (200 OK)
        assertEquals(200, res.statusCode, "로그인 시 HTTP 상태 코드는 200이어야 합니다.");

        System.out.println("로그인 상태 코드: " + res.statusCode);
        System.out.println("로그인 응답 바디: " + res.body);

        // 4) 응답 바디 JSON 파싱
        JsonNode root = objectMapper.readTree(res.body);

        System.out.println("JsonNode_root: " + root);

        // 필수 필드: id, username, name, profileUrl (profileUrl은 null 혹은 빈 문자열일 수 있음)
        assertTrue(root.has("id"), "로그인 응답에 'id' 필드가 있어야 합니다.");
        assertTrue(root.has("username"), "로그인 응답에 'username' 필드가 있어야 합니다.");
        assertTrue(root.has("name"), "로그인 응답에 'name' 필드가 있어야 합니다.");

        // 서버가 반환한 사용자 ID를 저장
        createdUserId = root.get("id").asLong();
        assertEquals(testUsername, root.get("username").asText(), "반환된 username이 요청 시 사용자명과 동일해야 합니다.");
        assertEquals(testName, root.get("name").asText(), "반환된 name이 요청 시 name과 동일해야 합니다.");
    }

    @Test
    @Order(4)
    void testSignOut() throws Exception {
        HttpResponse res = sendPostRequest(BASE_URL + "/signout", null);
        System.out.println("로그아웃 상태 코드: " + res.statusCode);
        System.out.println("로그아웃 응답 바디: " + res.body);

        JsonNode root = objectMapper.readTree(res.body);

        if (res.statusCode == 200) {
            // ─── 정상 로그아웃 케이스 ───
            // 로그인 후에 JSESSIONID 쿠키가 잘 넘어가서 로그아웃이 되면 200 + {"message":"로그아웃 성공"} 반환
            assertTrue(root.has("message"), "200일 때는 'message' 필드가 있어야 합니다.");
            assertTrue(root.get("message").asText().contains("로그아웃 성공"),
                    "200일 때 '로그아웃 성공' 메시지가 와야 합니다. 실제: " + root.get("message").asText());
        }
        else if (res.statusCode == 400) {
            // ─── 세션이 없어서 로그아웃할 수 없는 케이스 ───
            // 이미 로그아웃되었거나, 로그인 자체가 실패해서 세션이 없으면 400 + {"message":"로그인된 세션이 없습니다."}
            assertTrue(root.has("message"), "400일 때도 'message' 필드가 있어야 합니다.");
            assertTrue(root.get("message").asText().contains("로그인된 세션이 없습니다"),
                    "400일 때 '로그인된 세션이 없습니다' 메시지가 와야 합니다. 실제: " + root.get("message").asText());
        }
        else {
            fail("로그아웃 시 예상치 못한 HTTP 상태 코드입니다: " + res.statusCode);
        }
    }

    // -------------------------------------------------------------------
    //  Helper Method: HTTP POST 요청 보내기
    // -------------------------------------------------------------------

    /**
     * 주어진 URL로 HTTP POST 요청을 보내고, 상태 코드와 응답 바디를 반환한다.
     *
     * @param urlString  요청 보낼 URL
     * @param jsonBody   JSON 바디 (null이면 바디 없이 요청)
     * @return HttpResponse(상태 코드, 바디 문자열)
     * @throws IOException
     */
    private static HttpResponse sendPostRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        System.out.println("conn : " +conn.getURL());

        // JSON 바디를 주는 경우
        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 쿠키 유지: HTTP 세션을 유지하려면, 여기서 쿠키 관리가 필요합니다.
        // 하지만 HttpURLConnection은 기본적으로 쿠키를 유지하지 않으므로,
        // 동일한 세션 쿠키를 사용하려면 CookieManager를 설정하거나,
        // 직접 'Cookie' 헤더를 관리해야 합니다.
        //
        // 간단한 테스트용으로는, 로그인 → 로그아웃 사이에 동일한 HttpURLConnection 인스턴스를 쓰거나,
        // java.net.CookieManager를 전역에 등록해서 자동으로 쿠키를 저장/전송하게 할 수 있습니다.
        //
        // 여기 예제에서는 전역 CookieManager를 한 번 설정해 두는 방식으로 구현하겠습니다.
        //
        // ※ 아래 코드는 최초 호출 시 CookieManager를 세팅합니다.
        if (cookieManager == null) {
            cookieManager = new java.net.CookieManager();
            java.net.CookieHandler.setDefault(cookieManager);
        }

        // 응답 코드
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

    // 전역 CookieManager: 로그인 후 얻은 세션 쿠키(JSESSIONID 등)를 유지하도록 함
    private static java.net.CookieManager cookieManager;

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
