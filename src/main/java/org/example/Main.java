package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.friend.dto.*;
import org.example.user.dto.SignInReqDto;
import org.example.user.dto.SignInResDto;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {

    private static final String BASE_USER_URL   = "http://localhost:8080/api/user";
    private static final String BASE_FRIEND_URL = "http://localhost:8080/api/friends";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Main tester = new Main();

//        // 3. 친구 추가 테스트 (hong123 → kim456)
//        // (사전에 kim456 계정이 반드시 회원가입되어 있어야 합니다.)
//
//        AddFriendReqDto addReq = new AddFriendReqDto("hong123", "jung123");
//        AddFriendResDto addRes = tester.sendAddFriend(addReq);
//        System.out.println("AddFriend 응답: " + (addRes != null
//                ? tester.objectMapper.writeValueAsString(addRes)
//                : "친구 추가 실패 또는 응답이 null입니다"));
//        System.out.println();

        // 5. 친구 삭제 테스트 (hong123 → kim456)
        System.out.println("=== 5. 친구 삭제(RemoveFriend) 테스트 ===");
        RemoveFriendReqDto removeReq = new RemoveFriendReqDto("hong123", "jung123");
        RemoveFriendResDto removeRes = tester.sendRemoveFriend(removeReq);
        System.out.println("RemoveFriend 응답: " + (removeRes != null
                ? tester.objectMapper.writeValueAsString(removeRes)
                : "친구 삭제 실패 또는 응답이 null입니다"));
        System.out.println();

        // 6. 최종 친구 목록 조회 (hong123)
        System.out.println("=== 6. 최종 친구 목록 조회(GetFriendsList) 테스트 ===");
        List<FriendInfo> finalFriendsList = tester.sendGetFriendList("hong123");
        if (finalFriendsList != null) {
            System.out.println("최종 친구 목록:");
            System.out.println(tester.objectMapper.writeValueAsString(finalFriendsList));
        } else {
            System.out.println("GetFriendsList 실패 또는 응답이 null입니다");
        }
    }


    private SignInResDto sendSignIn(SignInReqDto reqDto) throws IOException {
        String targetUrl = BASE_USER_URL + "/signin";

        System.out.println("targetUrl: " + targetUrl);
        System.out.println("sendSignIn() : 진입성공");
        String requestJson = objectMapper.writeValueAsString(reqDto);

        HttpURLConnection conn = createPostConnection(targetUrl, "application/json");
        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            return objectMapper.readValue(responseJson, SignInResDto.class);
        } else if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            System.err.println("SignIn(401): 아이디 또는 비밀번호가 올바르지 않습니다.");
            return null;
        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("SignIn 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }

    // 3) 친구 추가(AddFriend) 호출
    private AddFriendResDto sendAddFriend(AddFriendReqDto reqDto) throws IOException {
        String targetUrl = BASE_FRIEND_URL;

        System.out.println("targetUrl: " + targetUrl);
        System.out.println("sendAddFriend() : 진입성공");
        String requestJson = objectMapper.writeValueAsString(reqDto);

        HttpURLConnection conn = createPostConnection(targetUrl, "application/json");
        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            return objectMapper.readValue(responseJson, AddFriendResDto.class);
        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("AddFriend 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }
    // 4) 친구 목록 조회(GetFriendsList) 호출
    private List<FriendInfo> sendGetFriendList(String username) throws IOException {
        String targetUrl = BASE_FRIEND_URL + "?username=" + username;

        System.out.println("targetUrl: " + targetUrl);
        System.out.println("sendGetFriendList() : 진입성공");

        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            GetFriendsListResDto resDto = objectMapper.readValue(responseJson, GetFriendsListResDto.class);
            return resDto.getFriendsList();

        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("GetFriendsList 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }

    private RemoveFriendResDto sendRemoveFriend(RemoveFriendReqDto reqDto) throws IOException {
        String targetUrl = BASE_FRIEND_URL;

        System.out.println("targetUrl: " + targetUrl);
        System.out.println("sendRemoveFriend() : 진입성공");
        String requestJson = objectMapper.writeValueAsString(reqDto);

        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        System.out.println("status: " + status);
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            System.out.println("responseJson: " + responseJson);
            return objectMapper.readValue(responseJson, RemoveFriendResDto.class);
        } else {
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("RemoveFriend 오류(HTTP " + status + "): " + errorJson);
            return null;
        }
    }


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