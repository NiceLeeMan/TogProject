package org.example.test;

import org.example.config.TestApiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class UserAuthIntegrationTest {

    private static final String BASE_URL     = TestApiConfig.get("api.baseUrl");
    private static final String SIGNUP_PATH  = TestApiConfig.get("api.user.signup");
    private static final String SIGNIN_PATH  = TestApiConfig.get("api.user.signin");
    private static final String SIGNOUT_PATH = TestApiConfig.get("api.user.signout");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Map<String, String>> testUsers = new ArrayList<>();
    private CookieManager cookieManager;

    @BeforeAll
    void setUp() {
        System.out.println("Testing against: " + BASE_URL);
        cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        for (int i = 1; i <= 5; i++) {
            Map<String, String> u = new HashMap<>();
            u.put("username", "testUser_" + i);
            u.put("password", "pw@" + (1000 + i));
            u.put("name",     "더미유저" + i);
            testUsers.add(u);
        }
    }

    @RepeatedTest(5)
    @Order(1)
    void testSignUp(RepetitionInfo info) throws Exception {
        Map<String, String> payload = testUsers.get(info.getCurrentRepetition() - 1);
        HttpResponse res = sendPost(BASE_URL + SIGNUP_PATH,
                objectMapper.writeValueAsString(payload));
        JsonNode root = objectMapper.readTree(res.body);
        if (res.statusCode == 201) {
            Assertions.assertTrue(root.path("success").asBoolean());
        } else if (res.statusCode == 409) {
            Assertions.assertFalse(root.path("success").asBoolean());
        } else {
            Assertions.fail("Unexpected status: " + res.statusCode);
        }
    }

    @Test
    @Order(2)
    void testSignIn() throws Exception {
        Map<String, String> u = testUsers.get(0);
        String json = objectMapper.writeValueAsString(
                Map.of("username", u.get("username"), "password", u.get("password"))
        );

        HttpResponse res = sendPost(BASE_URL + SIGNIN_PATH, json);
        Assertions.assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        JsonNode info = root.path("userInfo");
        // 성공 플래그 확인

    }


    @Test
    @Order(3)
    void testSignOut() throws Exception {
        HttpResponse res = sendPost(BASE_URL + SIGNOUT_PATH, null);
        JsonNode root = objectMapper.readTree(res.body);
        System.out.println(res.statusCode);
        System.out.println(res.body);
        if (res.statusCode == 200) {
            Assertions.assertTrue(root.path("message").asText().contains("로그아웃"));
        } else if (res.statusCode == 400) {
            Assertions.assertTrue(root.path("message").asText().contains("세션이 없습니다"));
        } else {
            Assertions.fail("Unexpected status on sign-out: " + res.statusCode);
        }
    }

    private HttpResponse sendPost(String url, String jsonBody) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");

        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        if (!cookies.isEmpty()) {
            StringJoiner sj = new StringJoiner("; ");
            for (HttpCookie c : cookies) sj.add(c.getName() + "=" + c.getValue());
            conn.setRequestProperty("Cookie", sj.toString());
        }

        int status = conn.getResponseCode();
        InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
        }
        return new HttpResponse(status, sb.toString());
    }

    private static class HttpResponse {
        final int statusCode;
        final String body;
        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
