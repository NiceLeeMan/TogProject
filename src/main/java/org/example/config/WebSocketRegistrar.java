package org.example.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

public class WebSocketRegistrar {
    public static void register(ServletContextHandler context,
                                String wsPath,
                                Class<?> endpointClass) {
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, container) -> {
            ServerEndpointConfig cfg = ServerEndpointConfig.Builder
                    .create(endpointClass, wsPath)
                    .configurator(new ServerEndpointConfig.Configurator())
                    .build();
            container.addEndpoint(cfg);
        });
    }
}
