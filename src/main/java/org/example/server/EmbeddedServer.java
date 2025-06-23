package org.example.server;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.example.config.ApiPathConfig;
import org.example.config.ServletRegistrar;
import org.example.config.TestApiConfig;
import org.example.config.WebSocketRegistrar;
import org.example.message.controller.MessageController;

import java.net.InetSocketAddress;

public class EmbeddedServer {
    private final Server server;

    public EmbeddedServer(TestApiConfig config) throws Exception {
        ApiPathConfig apiPaths = new ApiPathConfig(config.getProperties(), config);

        server = new Server(new InetSocketAddress("0.0.0.0", config.getPort()));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Register HTTP servlets
        new ServletRegistrar(context).registerAll(apiPaths);

        // Register WebSocket endpoint
        WebSocketRegistrar.register(context, apiPaths.getWsPath(), MessageController.class);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws InterruptedException {
        server.join();
    }
    public static void main(String[] args) throws Exception {
        TestApiConfig config = new TestApiConfig();
        EmbeddedServer server = new EmbeddedServer(config);
        server.start();

        String baseUrl = TestApiConfig.get("api.baseUrl");
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
        System.out.println("웹소켓(ws path)      : wss://" + config.getHost() + config.getWsPath());
        System.out.println("========================");

        server.join();
    }
}