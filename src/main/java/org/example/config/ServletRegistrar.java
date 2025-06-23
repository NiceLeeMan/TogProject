package org.example.config;

import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.example.chat.controller.ChatController;
import org.example.friend.controller.FriendController;
import org.example.memo.controller.MemoController;
import org.example.message.controller.MessageRestController;
import org.example.user.authentication.controller.SignInController;
import org.example.user.registration.controller.SignUpController;
import org.example.user.session.controller.SignOutController;

public class ServletRegistrar {
    private final ServletContextHandler context;

    public ServletRegistrar(ServletContextHandler context) {
        this.context = context;
    }

    private void register(Class<? extends HttpServlet> servletClass, String pathSpec) {
        context.addServlet(servletClass, pathSpec);
    }

    public void registerAll(ApiPathConfig paths) {
        register(SignUpController.class,   paths.getSignUpPath());
        register(SignInController.class,   paths.getSignInPath());
        register(SignOutController.class,  paths.getSignOutPath());

        register(FriendController.class,   paths.getFriendsPath());
        register(ChatController.class,     paths.getChatPath());
        register(MemoController.class,     paths.getMemoPath());
        register(MessageRestController.class, paths.getMessagePath());
    }
}
