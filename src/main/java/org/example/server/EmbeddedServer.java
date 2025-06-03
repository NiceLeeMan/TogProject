package org.example.server;

import org.example.friend.controller.FriendController;
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

            // UserController 서블릿 매핑
            context.addServlet(UserController.class, "/api/user/*");

            //friendController매핑
            context.addServlet(FriendController.class, "/api/friends/*");

            // 서버 시작
            server.start();
            System.out.println("==== Embedded Jetty Server Started ====");
            System.out.println("-> http://localhost:" + port + "/api/user/*");
            System.out.println("-> http://localhost:" + port + "/api/friends/*");
            // 종료까지 대기
            server.join();
        }

}
