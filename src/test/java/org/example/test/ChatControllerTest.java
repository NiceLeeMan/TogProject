package org.example.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatController 통합 테스트 (기존 예시 유저를 실제 유저 테이블의 사용자로 변경)
 *
 * - 서버 주소: http://localhost:8080
 * - API 경로:
 *     • 1:1 채팅방 생성    : POST /api/chat/one-to-one/create
 *     • 그룹 채팅방 생성   : POST /api/chat/group/create
 *     • 채팅방 나가기     : POST /api/chat/leave
 *     • 채팅방 재입장     : POST /api/chat/join
 *     • 메시지 전송       : POST /api/chat/send   (1:1 재가입용)
 *
 * 반드시 테스트를 실행하기 전에 Jetty 서버(EmbeddedServer)가 기동 중이어야 합니다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatControllerTest {

    private static final String BASE_URL_CHAT    = "http://localhost:8080/api/chat";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 실제 유저 테이블에 이미 존재하는 사용자들 (비밀번호 없음)
    // 7,   이기백,    user_1749109876, qwe@123,      0
    // 9,   서정춘,    user_1749110000, qwe@1234,     0
    // 13,  고종화,    qwe12345,        qwe@12345,    1
    // 14,  김덕민,    qwe123456,       qwe@123456,   0
    // 15,  정태우,    qwe1234567,      qwe@1234567,  0
    // 16,  김준호,    qwe12345678,     qwe@12345678, 0
    // 17,  정태우,    qwe123457,       qwe@123457,   0

    private static final String userA = "user_1749109876"; // 이기백
    private static final String userB = "user_1749110000"; // 서정춘
    private static final String userC = "qwe12345";        // 고종화
    private static final String userD = "qwe123456";       // 김덕민
    private static final String userE = "qwe1234567";      // 정태우

    private static java.net.CookieManager cookieManager;
    private static Long oneToOneRoomId;
    private static Long groupRoomId;

    @BeforeAll
    static void setUp() {
        // CookieManager 설정 (로그인은 필요 없으므로 생략)
        cookieManager = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(cookieManager);
    }

    /**
     * 1. 1:1 채팅방 생성
     */
    @Test
    @Order(1)
    void testCreateOneToOneChat() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", userA);
        payload.put("friendUsername", userB);

        HttpResponse res = sendPostRequest(BASE_URL_CHAT + "/one-to-one/create",
                objectMapper.writeValueAsString(payload));

        assertEquals(200, res.statusCode, "1:1 채팅방 생성 시 HTTP 상태 코드는 200이어야 합니다.");

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("chatRoomId"), "응답에 'chatRoomId' 필드가 있어야 합니다.");
        assertTrue(root.has("chatRoomName"), "응답에 'chatRoomName' 필드가 있어야 합니다.");
        assertTrue(root.has("members"), "응답에 'members' 필드가 있어야 합니다.");
        assertTrue(root.has("messages"), "응답에 'messages' 필드가 있어야 합니다.");

        oneToOneRoomId = root.get("chatRoomId").asLong();
        JsonNode members = root.get("members");
        assertTrue(members.isArray(), "'members'는 배열이어야 합니다.");
        assertEquals(2, members.size(), "1:1 방 생성 후 멤버 수는 2여야 합니다.");
        JsonNode messages = root.get("messages");
        assertTrue(messages.isArray(), "'messages'는 배열이어야 합니다.");
        assertEquals(0, messages.size(), "생성 직후 메시지 내역은 비어 있어야 합니다.");
    }

    /**
     * 2. 그룹 채팅방 생성 (초대 인원 3명: B, C, D)
     */
    @Test
    @Order(2)
    void testCreateGroupChat() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", userA);
        payload.put("chatRoomName", "GroupRoom1");

        List<Map<String, String>> members = new ArrayList<>();
        members.add(Map.of("username", userB));
        members.add(Map.of("username", userC));
        members.add(Map.of("username", userD));
        payload.put("members", members);

        HttpResponse res = sendPostRequest(BASE_URL_CHAT + "/group/create",
                objectMapper.writeValueAsString(payload));

        assertEquals(200, res.statusCode, "그룹 채팅방 생성 시 HTTP 상태 코드는 200이어야 합니다.");

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("chatRoomId"), "응답에 'chatRoomId' 필드가 있어야 합니다.");
        assertTrue(root.has("chatRoomName"), "응답에 'chatRoomName' 필드가 있어야 합니다.");
        assertTrue(root.has("members"), "응답에 'members' 필드가 있어야 합니다.");
        assertTrue(root.has("messages"), "응답에 'messages' 필드가 있어야 합니다.");

        groupRoomId = root.get("chatRoomId").asLong();
        JsonNode membersNode = root.get("members");
        assertTrue(membersNode.isArray(), "'members'는 배열이어야 합니다.");
        // 총 4명 (A, B, C, D)
        assertEquals(4, membersNode.size(), "그룹방 생성 후 멤버 수는 4여야 합니다.");
        JsonNode messages = root.get("messages");
        assertTrue(messages.isArray(), "'messages'는 배열이어야 합니다.");
        assertEquals(0, messages.size(), "생성 직후 메시지 내역은 비어 있어야 합니다.");
    }

    /**
     * 3. 1:1 채팅방 나가기 (B가 나감)
     */
    @Test
    @Order(3)
    void testLeaveOneToOneChat() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chatRoomId", oneToOneRoomId);
        payload.put("username", userB);

        HttpResponse res = sendPostRequest(BASE_URL_CHAT + "/leave",
                objectMapper.writeValueAsString(payload));

        assertEquals(200, res.statusCode, "1:1 방 나가기 시 HTTP 상태 코드는 200이어야 합니다.");

        JsonNode root = objectMapper.readTree(res.body);
        assertTrue(root.has("chatRoomId"), "응답에 'chatRoomId' 필드가 있어야 합니다.");
        assertTrue(root.has("username"), "응답에 'username' 필드가 있어야 합니다.");
        assertTrue(root.has("leftAt"), "응답에 'leftAt' 필드가 있어야 합니다.");
        assertTrue(root.has("members"), "응답에 'members' 필드가 있어야 합니다.");
        assertTrue(root.has("deleted"), "응답에 'deleted' 필드가 있어야 합니다.");

        JsonNode members = root.get("members");
        assertTrue(members.isArray(), "'members'는 배열이어야 합니다.");
        assertEquals(1, members.size(), "B가 나간 뒤 남은 멤버 수는 1이어야 합니다.");
        assertFalse(root.get("deleted").asBoolean(), "'deleted'는 false여야 합니다.");
    }

    /**
     * 4. 그룹 채팅방 나가기
     *   4-1) 개별 멤버 C가 나가기
     *   4-2) 마지막 멤버까지 전부 나가서 방 사라짐 (A, B, D 차례로 나감)
     */
    @Test
    @Order(4)
    void testLeaveGroupChat() throws Exception {
        // 4-1) C가 나가기
        Map<String, Object> payloadC = new HashMap<>();
        payloadC.put("chatRoomId", groupRoomId);
        payloadC.put("username", userC);

        HttpResponse resC = sendPostRequest(BASE_URL_CHAT + "/leave",
                objectMapper.writeValueAsString(payloadC));

        assertEquals(200, resC.statusCode, "그룹방 개별 나가기 시 HTTP 상태 코드는 200이어야 합니다.");

        JsonNode rootC = objectMapper.readTree(resC.body);
        assertTrue(rootC.has("members"), "응답에 'members' 필드가 있어야 합니다.");
        JsonNode membersC = rootC.get("members");
        assertTrue(membersC.isArray(), "'members'는 배열이어야 합니다.");
        assertEquals(3, membersC.size(), "C가 나간 뒤 남은 멤버 수는 3이어야 합니다.");
        assertFalse(rootC.get("deleted").asBoolean(), "'deleted'는 false여야 합니다.");

        // 4-2) 나머지 멤버 A, B, D 순서로 모두 나가기
        List<String> leaveOrder = List.of(userA, userB, userD);
        for (int i = 0; i < leaveOrder.size(); i++) {
            String username = leaveOrder.get(i);
            Map<String, Object> payload = new HashMap<>();
            payload.put("chatRoomId", groupRoomId);
            payload.put("username", username);

            HttpResponse res = sendPostRequest(BASE_URL_CHAT + "/leave",
                    objectMapper.writeValueAsString(payload));

            assertEquals(200, res.statusCode, "그룹방 나가기 시 HTTP 상태 코드는 200이어야 합니다.");

            JsonNode root = objectMapper.readTree(res.body);
            if (i < leaveOrder.size() - 1) {
                // 아직 멤버가 남아 있는 경우
                JsonNode members = root.get("members");
                assertTrue(members.isArray(), "'members'는 배열이어야 합니다.");
                assertEquals(leaveOrder.size() - 1 - i, members.size(),
                        "나간 뒤 남은 멤버 수가 맞지 않습니다.");
                assertFalse(root.get("deleted").asBoolean(), "'deleted'는 false여야 합니다.");
            } else {
                // 마지막 멤버가 나간 경우
                JsonNode members = root.get("members");
                assertTrue(members.isArray(), "'members'는 배열이어야 합니다.");
                assertEquals(0, members.size(), "마지막 멤버 나간 뒤 멤버 목록은 비어 있어야 합니다.");
                assertTrue(root.get("deleted").asBoolean(), "'deleted'는 true여야 합니다.");
            }
        }
    }

    /**
     * 5. 채팅방 재입장하기 (1:1 방 재가입 시나리오)
     *   • B가 나간 뒤 A가 메시지를 보내면 B는 자동 복구 → 이후 B가 /join 호출하여 메시지 내역 확인
     */
    @Test
    @Order(5)
    void testRejoinOneToOneChat() throws Exception {
        // 5-1) A가 B에게 메시지 전송 (자동 복구 트리거)
        Map<String, Object> sendPayload = new HashMap<>();
        sendPayload.put("chatRoomId", oneToOneRoomId);
        sendPayload.put("senderUsername", userA);
        sendPayload.put("content", "안녕하세요 B님!");

        HttpResponse sendRes = sendPostRequest(BASE_URL_CHAT + "/send",
                objectMapper.writeValueAsString(sendPayload));
        assertEquals(200, sendRes.statusCode, "메시지 전송 시 HTTP 상태 코드는 200이어야 합니다.");

        // 5-2) B가 채팅방 재입장 (/join)
        Map<String, Object> joinPayload = new HashMap<>();
        joinPayload.put("chatRoomId", oneToOneRoomId);
        joinPayload.put("username", userB);

        HttpResponse joinRes = sendPostRequest(BASE_URL_CHAT + "/join",
                objectMapper.writeValueAsString(joinPayload));
        assertEquals(200, joinRes.statusCode, "채팅방 재입장 시 HTTP 상태 코드는 200이어야 합니다.");

        JsonNode root = objectMapper.readTree(joinRes.body);
        assertTrue(root.has("members"), "응답에 'members' 필드가 있어야 합니다.");
        assertTrue(root.has("messages"), "응답에 'messages' 필드가 있어야 합니다.");

        JsonNode members = root.get("members");
        assertTrue(members.isArray(), "'members'는 배열이어야 합니다.");
        // A와 B 둘 다 active 멤버여야 함
        assertEquals(2, members.size(), "재입장 후 멤버 수는 2이어야 합니다.");

        JsonNode messages = root.get("messages");
        assertTrue(messages.isArray(), "'messages'는 배열이어야 합니다.");
        assertEquals(1, messages.size(), "재입장 후 메시지 내역 개수는 1이어야 합니다.");
        assertEquals("안녕하세요 B님!", messages.get(0).get("contents").asText(),
                "메시지 내용이 일치해야 합니다.");
    }

    // -------------------------------------------------------------------
    //  Helper Method: HTTP POST 요청 보내기
    // -------------------------------------------------------------------
    private static HttpResponse sendPostRequest(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        // JSON 바디 설정
        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 쿠키 유지: 전역 CookieManager 설정에 의해 자동 관리됨
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
