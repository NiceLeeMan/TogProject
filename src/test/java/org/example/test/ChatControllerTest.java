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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatControllerTest {

    private static final String BASE_URL = TestApiConfig.get("api.baseUrl");
    private static final String PATH_ONE_TO_ONE = TestApiConfig.get("api.chat.oneToOne");
    private static final String PATH_GROUP = TestApiConfig.get("api.chat.group");
    private static final String PATH_JOIN = TestApiConfig.get("api.chat.join");
    private static final String PATH_LEAVE = TestApiConfig.get("api.chat.leave");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String userA = "testUser_1"; // id=19
    private static final String userB = "testUser_2"; // id=20
    private static final String userC = "testUser_3"; // id=21
    private static final String userD = "testUser_4"; // id=22   // 정태우

    private static CookieManager cookieManager;
    private Long oneToOneRoomId;
    private Long groupRoomId;

    @BeforeAll
    static void setUp() throws Exception {
        // 1) CookieManager 설정 (로그인 세션 유지용)
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    /**
     * 1. 1:1 채팅방 생성
     */
    @Test
    @Order(1)
    void testCreateOneToOneChat() throws Exception {
        var req = Map.of(
                "username",       userA,
                "friendUsername", userB
        );
        var res = sendPost(BASE_URL + PATH_ONE_TO_ONE, objectMapper.writeValueAsString(req));

        System.out.println("testCreateOneToOneChat res.body = " + res.body);
        assertEquals(200, res.statusCode);
        var root = objectMapper.readTree(res.body);
        oneToOneRoomId = root.get("chatRoomId").asLong();
        assertEquals(2, root.get("members").size());
        assertEquals(0, root.get("messages").size());
    }

    /**
     * 2. 그룹 채팅방 생성
     */
    @Test
    @Order(2)
    void testCreateGroupChat() throws Exception {
        Map<String,Object> req = new HashMap<>();
        req.put("username", userA);
        req.put("chatRoomName", "GroupTest");
        req.put("members", List.of(
                Map.of("username", userB),
                Map.of("username", userC),
                Map.of("username", userD)
        ));
        var res = sendPost(BASE_URL + PATH_GROUP, objectMapper.writeValueAsString(req));

        System.out.println("testCreateGroupChat res.body = " + res.body);
        assertEquals(200, res.statusCode);
        var root = objectMapper.readTree(res.body);
        groupRoomId = root.get("chatRoomId").asLong();
        assertEquals(4, root.get("members").size());
        assertEquals(0, root.get("messages").size());
    }


    /** 3. 채팅방 입장 (1:1 & 그룹 공통) */
    @Test
    @Order(3)
    void testJoinChat() throws Exception {
        // 3-1) 1:1 방 입장: userB 재입장
        var join1 = Map.of(
                "chatRoomId", oneToOneRoomId.toString(),
                "username",   userB
        );
        var res1 = sendPost(BASE_URL + PATH_JOIN, objectMapper.writeValueAsString(join1));

        System.out.println("testJoinChat (1:1) res1.body = " + res1.body);

        assertEquals(200, res1.statusCode);
        var root1 = objectMapper.readTree(res1.body);
        assertEquals(2, root1.get("members").size());

        // 3-2) 그룹 방 입장: userC 재입장
        var join2 = Map.of(
                "chatRoomId", groupRoomId.toString(),
                "username",   userC
        );
        var res2 = sendPost(BASE_URL + PATH_JOIN, objectMapper.writeValueAsString(join2));

        System.out.println("testJoinChat (그룹) res2.body = " + res2.body);

        assertEquals(200, res2.statusCode);
        var root2 = objectMapper.readTree(res2.body);
        assertEquals(4, root2.get("members").size(), "그룹 방 입장 후에도 멤버 수는 유지되어야 합니다.");
    }

    /** 4. 채팅방 나가기 (1:1 & 그룹 공통) */
    @Test
    @Order(4)
    void testLeaveChat() throws Exception {
        // 4-1) 1:1 방 나가기: userB 나감
        var leave1 = Map.of(
                "chatRoomId", oneToOneRoomId.toString(),
                "username",   userB
        );
        var res1 = sendPost(BASE_URL + PATH_LEAVE, objectMapper.writeValueAsString(leave1));
        assertEquals(200, res1.statusCode);

        System.out.println("testLeaveChat (1:1) res1.body = " + res1.body);

        var root1 = objectMapper.readTree(res1.body);
        assertEquals(1, root1.get("members").size());

        // 4-2) 그룹 방 나가기: userE 나감
        var leave2 = Map.of(
                "chatRoomId", groupRoomId.toString(),
                "username",   userD
        );
        var res2 = sendPost(BASE_URL + PATH_LEAVE, objectMapper.writeValueAsString(leave2));

        System.out.println("testLeaveChat (그룹) res2.body = " + res2.body);

        assertEquals(200, res2.statusCode);
        var root2 = objectMapper.readTree(res2.body);
        assertEquals(3, root2.get("members").size());
    }



    // -------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------
    private HttpResponse sendPost(String url, String body) throws IOException {
        var conn = (HttpURLConnection)new URL(url).openConnection();
        conn.setRequestMethod("POST");
        if (body != null) {
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try(var os = conn.getOutputStream()) { os.write(body.getBytes(StandardCharsets.UTF_8)); }
        }
        int status = conn.getResponseCode();
        InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
        var sb = new StringBuilder();
        if (is != null) try (var br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line; while ((line = br.readLine()) != null) sb.append(line);
        }
        return new HttpResponse(status, sb.toString());
    }

    private record HttpResponse(int statusCode, String body) {}
}