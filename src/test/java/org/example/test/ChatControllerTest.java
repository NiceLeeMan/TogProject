package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.TestApiConfig;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatController 통합 테스트
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatControllerTest {

    private static final String BASE_URL              = TestApiConfig.get("api.baseUrl");
    private static final String PATH_ONE_TO_ONE       = TestApiConfig.get("api.chat.oneToOne");
    private static final String PATH_GROUP            = TestApiConfig.get("api.chat.group");
    private static final String PATH_JOIN             = TestApiConfig.get("api.chat.join");
    private static final String PATH_LEAVE            = TestApiConfig.get("api.chat.leave");
    private static final String PATH_GET_JOINED_ROOMS = TestApiConfig.get("api.chat.getJoinedRooms");
    private static final String PATH_SEND             = TestApiConfig.get("api.chat.send");

    private static Long oneToOneRoomId;
    private static Long groupRoomId;
    private static final ObjectMapper objectMapper    = new ObjectMapper();

    private static final String userA = "testUser_1"; // id=19
    private static final String userB = "testUser_2"; // id=20
    private static final String userC = "testUser_3"; // id=21
    private static final String userD = "testUser_4"; // id=22

    @BeforeAll
    void setUp() {
        // 필요한 초기화 (예: 쿠키 매니저 설정) 수행
    }

    @Test
    @Order(1)
    void testCreateOneToOneChat() throws IOException {
        Map<String,String> req = Map.of(
                "username",       userA,
                "friendUsername", userB
        );
        HttpResponse res = sendPost(BASE_URL + PATH_ONE_TO_ONE, objectMapper.writeValueAsString(req));
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        oneToOneRoomId = root.get("chatRoomId").asLong();
        assertEquals(2, root.get("members").size());
        assertEquals(0, root.get("messages").size());
    }

    @Test
    @Order(2)
    void testCreateGroupChat() throws IOException {
        Map<String,Object> req = new HashMap<>();
        req.put("username",     userA);
        req.put("chatRoomName", "GroupTest");
        req.put("members", List.of(
                Map.of("username", userB),
                Map.of("username", userC),
                Map.of("username", userD)
        ));
        HttpResponse res = sendPost(BASE_URL + PATH_GROUP, objectMapper.writeValueAsString(req));
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        groupRoomId = root.get("chatRoomId").asLong();
        assertEquals(4, root.get("members").size());
        assertEquals(0, root.get("messages").size());
    }

    @Test
    @Order(3)
    void testGetJoinedRooms() throws IOException {
        String url = BASE_URL + PATH_GET_JOINED_ROOMS
                + "?username=" + URLEncoder.encode(userA, StandardCharsets.UTF_8);
        HttpResponse res = sendGet(url);
        assertEquals(200, res.statusCode);

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("rooms"));
        JsonNode rooms = root.get("rooms");
        assertTrue(rooms.isArray());
        assertTrue(rooms.size() >= 2);
        JsonNode first = rooms.get(0);
        assertTrue(first.has("roomId"));
        assertTrue(first.has("roomName"));
        assertTrue(first.has("createdAt"));
    }

    @Test
    @Order(4)
    void testJoinChat() throws IOException {
        // ─── 1:1 방 ─────────────────────────────────────
        // 메시지 한 건 삽입
        Map<String,Object> sendReq1 = Map.of(
                "chatRoomId", oneToOneRoomId,
                "senderId",   19L,
                "contents",  "Hello OneToOne"
        );
        HttpResponse sendRes1 = sendPost(BASE_URL + PATH_SEND, objectMapper.writeValueAsString(sendReq1));
        assertEquals(201, sendRes1.statusCode);

        // userB 재입장
        Map<String,String> join1 = Map.of(
                "chatRoomId", oneToOneRoomId.toString(),
                "username",  userB
        );
        HttpResponse res1 = sendPost(BASE_URL + PATH_JOIN, objectMapper.writeValueAsString(join1));
        assertEquals(200, res1.statusCode);

        JsonNode root1 = objectMapper.readTree(res1.body);
        assertEquals(oneToOneRoomId.longValue(), root1.get("chatRoomId").asLong());
        assertTrue(root1.get("members").isArray() && root1.get("members").size() == 2);

        JsonNode joinMsgs1 = root1.get("messages");
        assertTrue(joinMsgs1.isArray());
        assertEquals(1, joinMsgs1.size());
        assertEquals("Hello OneToOne", joinMsgs1.get(0).get("content").asText());

        // ─── 그룹 방 ─────────────────────────────────────
        // 메시지 한 건 삽입
        Map<String,Object> sendReq2 = Map.of(
                "chatRoomId", groupRoomId,
                "senderId",   19L,
                "contents",  "Hello Group"
        );
        HttpResponse sendRes2 = sendPost(BASE_URL + PATH_SEND, objectMapper.writeValueAsString(sendReq2));
        assertEquals(201, sendRes2.statusCode);

        // userC 재입장
        Map<String,String> join2 = Map.of(
                "chatRoomId", groupRoomId.toString(),
                "username",  userC
        );
        HttpResponse res2 = sendPost(BASE_URL + PATH_JOIN, objectMapper.writeValueAsString(join2));
        assertEquals(200, res2.statusCode);

        JsonNode root2 = objectMapper.readTree(res2.body);
        assertEquals(groupRoomId.longValue(), root2.get("chatRoomId").asLong());
        assertTrue(root2.get("members").isArray() && root2.get("members").size() == 4);

        JsonNode joinMsgs2 = root2.get("messages");
        assertTrue(joinMsgs2.isArray());
        assertEquals(1, joinMsgs2.size());
        assertEquals("Hello Group", joinMsgs2.get(0).get("content").asText());
    }

    @Test
    @Order(5)
    void testLeaveChat() throws IOException {
        // 1:1 방 나가기
        Map<String,String> leave1 = Map.of(
                "chatRoomId", oneToOneRoomId.toString(),
                "username",  userB
        );
        HttpResponse res1 = sendPost(BASE_URL + PATH_LEAVE, objectMapper.writeValueAsString(leave1));
        assertEquals(200, res1.statusCode);
        JsonNode root1 = objectMapper.readTree(res1.body);
        assertEquals(1, root1.get("members").size());

        // 그룹 방 나가기
        Map<String,String> leave2 = Map.of(
                "chatRoomId", groupRoomId.toString(),
                "username",  userD
        );
        HttpResponse res2 = sendPost(BASE_URL + PATH_LEAVE, objectMapper.writeValueAsString(leave2));
        assertEquals(200, res2.statusCode);
        JsonNode root2 = objectMapper.readTree(res2.body);
        assertEquals(3, root2.get("members").size());
    }

    // -------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------
    private HttpResponse sendPost(String url, String body) throws IOException {
        return sendRequest(url, "POST", body);
    }

    private HttpResponse sendGet(String url) throws IOException {
        return sendRequest(url, "GET", null);
    }

    private HttpResponse sendRequest(String urlString, String method, String body) throws IOException {
        System.out.println("sendRequest [" + method + "] url = " + urlString);
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
        InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
        }

        return new HttpResponse(status, sb.toString());
    }

    private record HttpResponse(int statusCode, String body) {}
}
