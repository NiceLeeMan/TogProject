package org.example.server;


import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.example.chat.controller.ChatController;
import org.example.friend.controller.FriendController;
import org.example.memo.controller.MemoController;
import org.example.message.controller.MessageController;
import org.example.user.controller.UserController;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.InputStream;
import java.util.Properties;


public class EmbeddedServer {
    public static void main(String[] args) throws Exception {
        // 1) config.properties 로드
        Properties props = new Properties();
        try (InputStream in = EmbeddedServer.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(in);
        }

        // 2) 설정값 읽기
        int port = Integer.parseInt(props.getProperty("server.port", "8080"));
        String userPath    = props.getProperty("servlet.user");
        String friendsPath = props.getProperty("servlet.friends");
        String chatPath    = props.getProperty("servlet.chat");
        String memoPath    = props.getProperty("servlet.memo");
        String messagePath= props.getProperty("servlet.messages");  // 새로 추가
        String wsPath      = props.getProperty("ws.path");

        // 3) Jetty 서버 및 컨텍스트 설정
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // 4) 서블릿 매핑
        context.addServlet(UserController.class,   userPath);
        context.addServlet(FriendController.class, friendsPath);
        context.addServlet(ChatController.class,   chatPath);
        context.addServlet(MemoController.class,   memoPath);

        // 5) WebSocket 엔드포인트 등록
        JakartaWebSocketServletContainerInitializer.configure(
                context,
                (servletContext, wsContainer) -> {
                    wsContainer.addEndpoint(MessageController.class);
                }
        );

        // 6) 서버 시작
        server.start();

        // 7) 시작 정보 출력
        System.out.println("==== Embedded Jetty Server Started ====\nHTTP Endpoints:");
        System.out.printf("-> UserController   : %s%n", userPath);
        System.out.printf("-> FriendController : %s%n", friendsPath);
        System.out.printf("-> ChatController   : %s%n", chatPath);
        System.out.printf("-> MemoController   : %s%n", memoPath);
        System.out.println();
        System.out.printf("WebSocket Endpoint : ws://localhost:%d%s%n", port, wsPath);
        System.out.printf("Server listening on port %d%n", port);

        // 8) 종료 대기
        server.join();
    }
}
