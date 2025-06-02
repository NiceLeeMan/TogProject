package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSoruceConfig {

    public static HikariDataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/chat_app?serverTimezone=UTC&useSSL=false");
        config.setUsername("root");
        config.setPassword("비밀번호");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // (Driver class는 커넥터 버전에 따라 "com.mysql.cj.jdbc.Driver"가 맞습니다.)
        return new HikariDataSource(config);
    }

}
