package org.example.server;

import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.example.config.TestApiConfig;
import org.example.user.controller.UserController;
import org.example.friend.controller.FriendController;
import org.example.chat.controller.ChatController;
import org.example.memo.controller.MemoController;
import org.example.message.controller.MessageRestController;
import org.example.message.controller.MessageController;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Embedded Jetty Server for both HTTP and WebSocket endpoints.
 */
public class EmbeddedServer {
    private final Server server;
    private final String userPath;
    private final String friendsPath;
    private final String chatPath;
    private final String memoPath;
    private final String messagePath;
    private final String wsPath;

    /**
     * Constructor for programmatic setup using TestApiConfig or properties.
     */
    public EmbeddedServer(TestApiConfig config) throws Exception {
        Properties props = config.getProperties();
        this.userPath    = props.getProperty("servlet.user");
        this.friendsPath = props.getProperty("servlet.friends");
        this.chatPath    = props.getProperty("servlet.chat");
        this.memoPath    = props.getProperty("servlet.memo");
        this.messagePath = props.getProperty("servlet.messages");
        this.wsPath = config.getWsPath();  // "/ws/chat/{chatRoomId}"


        int port = config.getPort();
        System.out.println(port);

        // ✅ 외부 요청도 수용하도록 전체 주소로 바인딩
        server = new Server(new InetSocketAddress("0.0.0.0", port));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // HTTP mappings
        context.addServlet(UserController.class,    userPath);
        context.addServlet(FriendController.class,  friendsPath);
        context.addServlet(ChatController.class,    chatPath);
        context.addServlet(MemoController.class,    memoPath);
        context.addServlet(MessageRestController.class, messagePath);

        // WebSocket endpoint (wsPath 프로퍼티 사용)


        System.out.println(wsPath);
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, container) -> {
            ServerEndpointConfig cfg = ServerEndpointConfig.Builder
                    .create(MessageController.class, "/ws/chat")
                    .configurator(new ServerEndpointConfig.Configurator())
                    .build();
            container.addEndpoint(cfg);

        });
    }

    /**
     * Starts the Jetty server.
     */
    public void start() throws Exception {
        server.start();
    }

    /**
     * Stops the Jetty server.
     */
    public void stop() throws Exception {
        server.stop();
    }

    /**
     * Blocks the current thread until the server is done.
     */
    public void join() throws InterruptedException {
        server.join();
    }

    /**
     * Legacy main method for standalone execution.
     */
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream in = EmbeddedServer.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(in);
        }
        TestApiConfig config = new TestApiConfig(props);
        EmbeddedServer server = new EmbeddedServer(config);
        server.start();


        String baseUrl = TestApiConfig.get("api.baseUrl");           // e.g. "http://localhost:8080"
        System.out.println("=== Application URLs ===");
        System.out.println("Base URL          : " + baseUrl);
        System.out.println("회원가입(sign-up)  : " + baseUrl + TestApiConfig.get("api.user.signup"));
        System.out.println("로그인(sign-in)    : " + baseUrl + TestApiConfig.get("api.user.signin"));
        System.out.println("로그아웃(sign-out) : " + baseUrl + TestApiConfig.get("api.user.signout"));
        System.out.println("친구추가(add friend) : " + baseUrl + TestApiConfig.get("api.friends.add"));
        System.out.println("친구삭제(delete friend) : " + baseUrl + TestApiConfig.get("api.friends.delete"));
        System.out.println("친구목록(list friends)  : " + baseUrl + TestApiConfig.get("api.friends.list"));
        System.out.println("메모작성(add memo)   : " + baseUrl + TestApiConfig.get("api.memo.add"));
        System.out.println("메모조회(get memo)   : " + baseUrl + TestApiConfig.get("api.memo.get"));
        System.out.println("메시지전송(send msg)  : " + baseUrl + TestApiConfig.get("api.messages.send"));
        System.out.println("메시지조회(fetch msg) : " + baseUrl + TestApiConfig.get("api.messages.fetch"));
        System.out.println("웹소켓(ws path)      : wss://"
                + config.getHost() + ":"
                + config.getWsPath());
        System.out.println("========================");


        server.join();
    }
}
