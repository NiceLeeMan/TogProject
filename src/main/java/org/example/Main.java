package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.user.dto.SignUpReqDto;
import org.example.user.dto.SignUpResDto;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final String BASE_URL = "http://localhost:8080/api/user";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Main tester = new Main();

        System.out.println("=== 1. 회원가입(SignUp) 테스트 ===");
        SignUpReqDto signUpReq = new SignUpReqDto("홍길동", "hong123", "password123");
        SignUpResDto signUpRes = tester.sendSignUp(signUpReq);
        System.out.println(signUpRes);
        System.out.println("SignUp 응답: " + tester.toJson(signUpRes));
        System.out.println();


    }

    private SignUpResDto sendSignUp(SignUpReqDto reqDto) throws IOException {
        String targetUrl = BASE_URL + "/signup";
        String requestJson = objectMapper.writeValueAsString(reqDto);

        HttpURLConnection conn = createPostConnection(targetUrl, "application/json");
        writeRequestBody(conn, requestJson);

        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            String responseJson = readResponseBody(conn.getInputStream());
            return objectMapper.readValue(responseJson, SignUpResDto.class);
        } else {
            System.out.println("status" + status);
            String errorJson = readResponseBody(conn.getErrorStream());
            System.err.println("SignUp 오류(HTTP " + status + "): " + errorJson);
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