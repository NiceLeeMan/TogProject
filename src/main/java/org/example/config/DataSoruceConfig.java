package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSoruceConfig {

    public static HikariDataSource getDataSource() {
        Properties props = new Properties();
        try (InputStream in = DataSoruceConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("❌ db.properties 파일을 찾을 수 없습니다!");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("⚠️ db.properties 로딩 실패", e);
        }

        String url = props.getProperty("jdbc.url");
        System.out.println("✅ JDBC URL 로딩됨: " + url); // 디버깅용

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        System.out.println("url: " + url);
        config.setUsername(props.getProperty("jdbc.username"));
        config.setPassword(props.getProperty("jdbc.password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }

}
