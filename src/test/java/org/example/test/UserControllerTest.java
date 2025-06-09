package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.TestApiConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.RepetitionInfo;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class UserControllerTest {

    private static final String BASE_URL    = TestApiConfig.get("api.baseUrl");
    private static final String SIGNUP_PATH  = TestApiConfig.get("api.user.signup");
    private static final String SIGNIN_PATH  = TestApiConfig.get("api.user.signin");
    private static final String SIGNOUT_PATH = TestApiConfig.get("api.user.signout");


    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 더미 유저 10명 생성용 리스트
    private static final List<Map<String, String>> testUsers = new ArrayList<>();

    // 테스트 도중 생성한 사용자 ID 저장
    private static final List<Long> createdUserIds = new ArrayList<>();

    private static CookieManager cookieManager;

    @BeforeAll
    static void setUp() {

        // 쿠키 매니저 초기화
        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(cookieManager);

        System.out.println("한번만 호출됨");

        // 10명의 더미 사용자 정보 생성
        for (int i = 1; i <= 10; i++) {
            String username = "testUser" + "_" + i;
            String password = "pw@" + (1000 + i);
            String name     = "더미유저" + i;
            Map<String, String> user = new HashMap<>();
            user.put("username", username);
            user.put("password", password);
            user.put("name",     name);
            testUsers.add(user);
        }
    }

    /**
     * 회원가입 테스트: 10명의 더미 유저를 순차적으로 등록
     */
    @RepeatedTest(value = 10, name = "회원가입 반복 {currentRepetition} / {totalRepetitions}")
    @Order(1)
    void testSignUp(RepetitionInfo info) throws Exception {
        int idx = info.getCurrentRepetition() - 1;
        Map<String, String> payload = testUsers.get(idx);

        HttpResponse res = sendPostRequest(
                BASE_URL + SIGNUP_PATH,
                objectMapper.writeValueAsString(payload)
        );

        System.out.println("[회원가입 #" + info.getCurrentRepetition() + "] 상태 코드: " + res.statusCode);
        System.out.println("[회원가입 #" + info.getCurrentRepetition() + "] 응답 바디: " + res.body);

        JsonNode root = objectMapper.readTree(res.body);
        if (res.statusCode == 201) {
            Assertions.assertTrue(root.get("success").asBoolean(), "success 플래그는 true여야 합니다.");
        } else if (res.statusCode == 409) {
            Assertions.assertFalse(root.get("success").asBoolean(), "중복 시 success 플래그는 false여야 합니다.");
        } else {
            Assertions.fail("예상치 못한 HTTP 상태 코드: " + res.statusCode);
        }
        // 신규 생성 ID 저장 (성공 시에만)
        if (root.has("id")) {
            createdUserIds.add(root.get("id").asLong());
        }
    }

    /**
     * 로그인 테스트: 첫 번째 생성된 사용자로 로그인
     */
    @Test
    @Order(2)
    void testSignIn() throws Exception {

        Map<String, String> firstUser = testUsers.get(7);
        Map<String, String> payload = Map.of(
                "username", firstUser.get("username"),
                "password", firstUser.get("password")
        );

        HttpResponse res = sendPostRequest(
                BASE_URL + SIGNIN_PATH,
                objectMapper.writeValueAsString(payload)
        );

        System.out.println("resBody: " + res.body + "\n" +"resStatus: " + res.statusCode);
        Assertions.assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        Assertions.assertTrue(root.has("id"));
        Assertions.assertEquals(firstUser.get("username"), root.get("username").asText());
    }

    /**
     * 로그아웃 테스트: 로그인한 세션으로 로그아웃
     */
    @Test
    @Order(3)
    void testSignOut() throws Exception {
        // 로그아웃 대상 사용자 정보 (첫 번째 사용자)
        Map<String, String> firstUser = testUsers.get(7);
        System.out.println("[로그아웃 테스트] 대상 사용자: " + firstUser.get("username"));

        // 세션 쿠키(JSESSIONID)는 testSignIn에서 이미 저장되어 있음
        HttpResponse res = sendPostRequest(
                BASE_URL + SIGNOUT_PATH,
                null
        );

        System.out.println("[로그아웃] 상태 코드: " + res.statusCode);
        System.out.println("[로그아웃] 응답 바디: " + res.body);

        JsonNode root = objectMapper.readTree(res.body);
        if (res.statusCode == 200) {
            Assertions.assertTrue(root.has("message"), "200일 때는 'message' 필드가 있어야 합니다.");
            Assertions.assertTrue(root.get("message").asText().contains("로그아웃 성공"),
                    "200일 때 '로그아웃 성공' 메시지가 와야 합니다. 실제: " + root.get("message").asText());
        } else if (res.statusCode == 400) {
            Assertions.assertTrue(root.has("message"), "400일 때도 'message' 필드가 있어야 합니다.");
            Assertions.assertTrue(root.get("message").asText().contains("로그인된 세션이 없습니다"),
                    "400일 때 '로그인된 세션이 없습니다' 메시지가 와야 합니다. 실제: " + root.get("message").asText());
        } else {
            Assertions.fail("예상치 못한 HTTP 상태 코드입니다: " + res.statusCode);
        }
    }


    // -------------------------------------------------------------------
    // Helper: HTTP POST 요청
    // -------------------------------------------------------------------
    private static HttpResponse sendPostRequest(String urlString, String jsonBody) throws IOException {
        // 0) 언제나 한 번만 @BeforeAll 에서 세팅된 cookieManager 가 기본 핸들러로 등록되어 있어야 합니다.

        CookieHandler.setDefault(cookieManager);

        System.out.println("[Before logout] Stored cookies: "
                + cookieManager.getCookieStore().getCookies());

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");

        // 1) JSON 바디 쓰기 (로그아웃처럼 body가 없으면 이 블록은 스킵)
        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        // 2) 수동으로 쿠키 헤더 달기 (자동 첨부가 안 될 경우 대비)
        // 쿠키 헤더 수동 설정 (필요시)
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        if (!cookies.isEmpty()) {
            StringJoiner sj = new StringJoiner("; ");
            for (HttpCookie c : cookies) sj.add(c.getName() + "=" + c.getValue());
            conn.setRequestProperty("Cookie", sj.toString());
        }

        // 3) 요청 전송 & 응답 코드 받기
        int status = conn.getResponseCode();

        // 4) 응답 본문 읽기
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder responseText = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseText.append(line);
                }
            }
        }

        // 5) 최종 저장된 쿠키 확인
        System.out.println("Stored cookies AFTER response: " + cookieManager.getCookieStore().getCookies());

        return new HttpResponse(status, responseText.toString());
    }

    private static class HttpResponse {
        int statusCode;
        String body;
        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
