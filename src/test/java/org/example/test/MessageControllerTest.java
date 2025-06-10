package org.example.test;

import jakarta.websocket.*;
import org.example.config.TestApiConfig;
import org.example.server.EmbeddedServer;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * MessageController의 onMessage(...) 브로드캐스팅 동작 단위 테스트
 */


/**
 * MessageController의 onMessage(...) 브로드캐스팅 동작 단위 테스트
 */
/**
 * MessageController의 onMessage(...) 브로드캐스팅 동작 단위 테스트
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageControllerTest {
    private static String WEBSOCKET_URL;
    private static CountDownLatch latch;
    private static String receivedMessage;
    private static EmbeddedServer server;

    @BeforeAll
    static void startServer() throws Exception {
        // TestApiConfig 기본 생성자로 config.properties와 api.properties 로드
        TestApiConfig config = new TestApiConfig();
        server = new EmbeddedServer(config);
        // WebSocket 엔드포인트 초기화 대기 (필요시)
        Thread.sleep(200);


        // ws.path를 읽어와 URL 구성
        String host = config.getHost();
        int port = config.getPort();
        String path = config.getWsPath();

        WEBSOCKET_URL = String.format("wss://%s%s?chatRoomId=%d", host, path, 56);
        System.out.println("테스트 WebSocket URL: " + WEBSOCKET_URL);
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @ClientEndpoint
    public static class TestClientEndpoint {
        @OnOpen
        public void onOpen(Session session) {
            System.out.println("클라이언트: 서버에 연결되었습니다. Session ID: " + session.getId());
        }

        @OnMessage
        public void onMessage(String message) {
            System.out.println("클라이언트: 메시지 수신: " + message);
            receivedMessage = message;
            latch.countDown();
        }

        @OnClose
        public void onClose(Session session, CloseReason reason) {

            System.out.println("클라이언트: 연결이 닫혔습니다. Reason: " + reason);

        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            System.err.println("클라이언트: 오류 발생");
            throwable.printStackTrace();
        }
    }

    @Test
    @Order(1)
    void testBroadcasting() throws Exception {
        latch = new CountDownLatch(1);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        System.out.println("container 연결성공: " + container);

        // 로컬 EmbeddedServer WebSocket endpoint에 연결
        Session session = container.connectToServer(TestClientEndpoint.class, new URI(WEBSOCKET_URL));
        System.out.println("session 연결 시도. isOpen: " + session.isOpen());

        // 테스트 메시지 전송
        String testJson = "{\"roomId\": 56, \"senderId\": 20, \"contents\": \"너나 나가!\"}";
        session.getBasicRemote().sendText(testJson);

        // 5초 내 메시지 수신 대기
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "시간 내에 서버로부터 메시지를 받지 못했습니다.");
        assertNotNull(receivedMessage, "수신된 메시지가 null입니다.");

        System.out.println("최종 수신 메시지: " + receivedMessage);
        session.close();
    }
}
