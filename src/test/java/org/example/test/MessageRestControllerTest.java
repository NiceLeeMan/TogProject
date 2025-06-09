package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.TestApiConfig;
import org.example.server.EmbeddedServer;
import org.junit.jupiter.api.*;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageRestControllerTest {

    private static EmbeddedServer server;
    private static TestApiConfig config;
    private static String baseUrl;
    private static String sendPath;
    private static String fetchPath;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void beforeAll() throws Exception {
        // 1) 설정 로드 및 서버 기동
        config = new TestApiConfig();                          // no-arg constructor로 config.properties 로드
                                            // 서버는 /api/messages/* 매핑 포함 :contentReference[oaicite:0]{index=0}
        // 2) URL 구성
        baseUrl   = TestApiConfig.get("api.baseUrl");          // e.g. http://localhost:8080 :contentReference[oaicite:1]{index=1}
        sendPath  = TestApiConfig.get("api.messages.send");    // "/api/messages/send" :contentReference[oaicite:2]{index=2}
        fetchPath = TestApiConfig.get("api.messages.fetch");   // "/api/messages"      :contentReference[oaicite:3]{index=3}
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    /** 1) 메시지 전송 테스트 */
    @Test
    @Order(1)
    void testSendMessage() throws Exception {
        SendMessageReq reqDto = new SendMessageReq();
        reqDto.setRoomId(56L);
        reqDto.setSenderId(19L);
        reqDto.setContents("안녕하세요!");

        System.out.println("reqDto = " + reqDto);

        // 요청 보내기
        String url = baseUrl + sendPath;

        HttpResponse res = sendPost(url, objectMapper.writeValueAsString(reqDto));
        System.out.println("url : " +url);
        System.out.println(">>> StatusCode : " + res.statusCode);
        System.out.println(">>> Body       : " + res.body);

        assertEquals(201, res.statusCode, "POST /api/messages/send는 201을 반환해야 합니다.");

        // 응답 JSON 검증
        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("msgId"),     "msgId 필드 존재");
        assertEquals(56L, root.get("chatRoomId").asLong());
        assertEquals(19L,   root.get("senderId").asLong());
        assertEquals("안녕하세요!", root.get("contents").asText());
    }

    /** 2) 메시지 조회 테스트 */
    @Test
    @Order(2)
    void testFetchMessages() throws Exception {
        // 이미 roomId=100에 메시지가 저장되어 있다고 가정하거나,
        // 앞선 testSendMessage가 저장한 메시지를 포함해 조회 가능해야 합니다.

        String url = String.format("%s%s?roomId=%d&username=%s",
                baseUrl, fetchPath, 56L, "testUser_1");

        HttpResponse res = sendGet(url);
        System.out.println("url : " +url);
        assertEquals(200, res.statusCode, "GET /api/messages?roomId=...&username=...는 200을 반환해야 합니다.");

        JsonNode arr = objectMapper.readTree(res.body);
        assertTrue(arr.isArray(), "응답은 JSON 배열이어야 합니다.");
        assertTrue(arr.size() >= 1, "최소 한 개 이상의 메시지가 반환되어야 합니다.");
        JsonNode first = arr.get(0);
        assertEquals(56L, first.get("chatRoomId").asLong());
        assertTrue(first.has("senderUsername"));
        assertTrue(first.has("contents"));
    }

    // -------------------------------------------------------------------
    //  HTTP 요청 헬퍼
    // -------------------------------------------------------------------
    private static HttpResponse sendPost(String urlStr, String jsonBody) throws IOException {
        URL url = new URL(urlStr);


        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);


        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        String body = readBody(conn, status);

        return new HttpResponse(status, body);
    }

    private static HttpResponse sendGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        String body = readBody(conn, status);
        System.out.println("body : " + body);
        return new HttpResponse(status, body);
    }

    private static String readBody(HttpURLConnection conn, int status) throws IOException {
        InputStream is = (status >= 200 && status < 400)
                ? conn.getInputStream()
                : conn.getErrorStream();
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static class HttpResponse {
        final int statusCode;
        final String body;
        HttpResponse(int s, String b) { this.statusCode = s; this.body = b; }
    }
}
