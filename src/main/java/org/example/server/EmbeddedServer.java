package org.example.server;


import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.example.chat.controller.ChatController;
import org.example.friend.controller.FriendController;
import org.example.memo.controller.MemoController;
import org.example.message.controller.MessageController;
import org.example.user.controller.UserController;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;


public class EmbeddedServer {

        public static void main(String[] args) throws Exception {
            int port = 8080;

            // Jetty Server 생성
            Server server = new Server(port);
            // ServletContextHandler 설정
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            context.addServlet(UserController.class,   "/api/user/*");
            context.addServlet(FriendController.class, "/api/friends/*");
            context.addServlet(ChatController.class,   "/api/chat/*");
            context.addServlet(MemoController.class,   "/api/memo/*");



            // ─── WebSocket 엔드포인트 등록 (Jetty 11 / Jakarta) ───
            JakartaWebSocketServletContainerInitializer.configure(
                    context,
                    (servletContext, wsContainer) -> {
                        // MessageController 의 @ServerEndpoint("/ws/chat/{chatRoomId}") 등록
                        wsContainer.addEndpoint(MessageController.class);
                    }
            );


            // 서버 시작
            server.start();

            System.out.println("==== Embedded Jetty Server Started ====");
            System.out.println("-> http://localhost:" + port + "/api/user/*");
            System.out.println("-> http://localhost:" + port + "/api/friends/*");
            System.out.println("-> http://localhost:" + port + "/api/chat/*");
            System.out.println("-> http://localhost:" + port + "/api/memo/*");

            System.out.println("Server started: HTTP on 8080, WS on ws://localhost:8080/ws/chat/{chatRoomId}");
            server.join();
            // 종료까지 대기
        }

}
