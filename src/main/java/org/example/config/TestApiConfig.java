package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestApiConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in =
                     TestApiConfig.class.getResourceAsStream("/api.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("api.properties 로드 실패: " + e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}