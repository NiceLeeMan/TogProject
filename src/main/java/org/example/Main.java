package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.chat.dto.*;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final String BASE_USER_URL   = "http://localhost:8080/api/user";
    private static final String BASE_FRIEND_URL = "http://localhost:8080/api/friends";
    private static final String BASE_CHAT_URL = "http://localhost:8080/api/chat";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        // LocalDateTime 필드를 포함한 DTO를 직렬화/역직렬화하려면 JavaTimeModule을 등록해야 합니다.
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);;

        Main tester = new Main();

        // 1. 일대일 채팅방 생성 테스트
        System.out.println("=== 1. 일대일 채팅방 생성(Create One-to-One Chat) 테스트 ===");
        CreateOneToOneChatReqDto oneToOneReq = new CreateOneToOneChatReqDto("hong123", "lee123");
        JoinChatResDto oneToOneRes = tester.sendCreateOneToOneChat(oneToOneReq);
        System.out.println("CreateOneToOneChat 응답: " + (oneToOneRes != null
                ? tester.objectMapper.writeValueAsString(oneToOneRes)
                : "응답이 null입니다"));
        System.out.println();


        // 2. 그룹 채팅방 생성 테스트
        System.out.println("=== 2. 그룹 채팅방 생성(Create Group Chat) 테스트 ===");
        MemberInfo member1 = new MemberInfo();
        member1.setUsername("hong123");
        MemberInfo member2 = new MemberInfo();
        member2.setUsername("lee123");
        MemberInfo member3 = new MemberInfo();
        member3.setUsername("jung123");
        List<MemberInfo> members = Arrays.asList(member1, member2, member3);

        CreateGroupChatReqDto groupReq = new CreateGroupChatReqDto("hong123", "SampleGroup", members);
        JoinChatResDto groupRes = tester.sendCreateGroupChat(groupReq);
        System.out.println("CreateGroupChat 응답: " + (groupRes != null
                ? tester.objectMapper.writeValueAsString(groupRes)
                : "응답이 null입니다"));
        System.out.println();


    }




    // ───────────────────────────────────────────────────────────────────────────
    // 1) 일대일 채팅방 생성(Create One-to-One Chat) 호출
    private JoinChatResDto sendCreateOneToOneChat(CreateOneToOneChatReqDto reqDto) throws IOException {
        String targetUrl = BASE_CHAT_URL + "/one-to-one/create";

        System.out.println("targetUrl: " + targetUrl);
        String requestJson = objectMapper.writeValueAsString(reqDto);

        HttpURLConnection conn = createPostConnection(targetUrl, "application/json");
        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            return objectMapper.readValue(responseJson, JoinChatResDto.class);
        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("CreateOneToOneChat 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 2) 그룹 채팅방 생성(Create Group Chat) 호출
    private JoinChatResDto sendCreateGroupChat(CreateGroupChatReqDto reqDto) throws IOException {
        String targetUrl = BASE_CHAT_URL + "/group/create";

        System.out.println("targetUrl: " + targetUrl);
        String requestJson = objectMapper.writeValueAsString(reqDto);

        HttpURLConnection conn = createPostConnection(targetUrl, "application/json");
        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            return objectMapper.readValue(responseJson, JoinChatResDto.class);

        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("CreateGroupChat 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 3) 채팅방 입장(Join Chat) 호출
    private JoinChatResDto sendJoinChat(JoinChatReqDto reqDto) throws IOException {
        String targetUrl = BASE_CHAT_URL + "/join";

        System.out.println("targetUrl: " + targetUrl);
        String requestJson = objectMapper.writeValueAsString(reqDto);

        HttpURLConnection conn = createPostConnection(targetUrl, "application/json");
        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            return objectMapper.readValue(responseJson, JoinChatResDto.class);
        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("JoinChat 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }

    // ───────────────────────────────────────────────────────────────────────────
    // 공통: POST 연결 생성
    private HttpURLConnection createPostConnection(String urlString, String contentType) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", contentType + "; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private void writeRequestBody(HttpURLConnection conn, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
            os.flush();
        }
    }

    private String readResponseBody(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String toJson(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }
}