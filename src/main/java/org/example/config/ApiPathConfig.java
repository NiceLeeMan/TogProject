package org.example.config;


import java.util.Properties;

public class ApiPathConfig {
    private final String signUpPath;
    private final String signInPath;
    private final String signOutPath;
    private final String friendsPath;
    private final String chatPath;
    private final String memoPath;
    private final String messagePath;
    private final String wsPath;

    public ApiPathConfig(Properties props, TestApiConfig config) {
               // 사용자 관련 3개 경로는 api.properties 에 정의된 키로 읽어온다.
                        this.signUpPath    = TestApiConfig.get("api.user.signup");
                this.signInPath    = TestApiConfig.get("api.user.signin");
                this.signOutPath   = TestApiConfig.get("api.user.signout");
                // 나머지 경로는 기존대로 config.properties 에 정의된 키로 읽는다.
                        this.friendsPath   = props.getProperty("servlet.friends");
                this.chatPath      = props.getProperty("servlet.chat");
                this.memoPath      = props.getProperty("servlet.memo");
                this.messagePath   = props.getProperty("servlet.messages");
                this.wsPath        = config.getWsPath();
            }

    public String getSignUpPath()   { return signUpPath; }
    public String getSignInPath()   { return signInPath; }
    public String getSignOutPath()  { return signOutPath; }
    public String getFriendsPath()  { return friendsPath; }
    public String getChatPath()     { return chatPath; }
    public String getMemoPath()     { return memoPath; }
    public String getMessagePath()  { return messagePath; }
    public String getWsPath()       { return wsPath; }
}
