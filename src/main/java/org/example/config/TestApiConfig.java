package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * TestApiConfig handles loading of both API endpoint definitions (api.properties)
 * and server configuration (config.properties) for EmbeddedServer in tests.
 */
public class TestApiConfig {
    private static final Properties apiProps = new Properties();
    private static final Properties configProps = new Properties();
    private final Properties instanceProps;

    static {
        try (InputStream apiIn = TestApiConfig.class.getClassLoader().getResourceAsStream("config/api.properties");
             InputStream configIn = TestApiConfig.class.getClassLoader().getResourceAsStream("config/config.properties")) {
            if (apiIn != null) apiProps.load(apiIn);
            if (configIn != null) configProps.load(configIn);
        } catch (IOException e) {
            throw new RuntimeException("설정 파일 로딩 실패", e);
        }
    }

    public TestApiConfig() {
        this.instanceProps = loadConfigProperties();
    }


    public TestApiConfig(Properties props) {
        this.instanceProps = props;
    }

    public static String get(String key) {
        return apiProps.getProperty(key);
    }

    public Properties getProperties() {
        return instanceProps;
    }

    public int getPort() {
        return Integer.parseInt(instanceProps.getProperty("server.port"));
    }

    public String getWsPath() {
        return instanceProps.getProperty("ws.path");
    }
    private static Properties loadConfigProperties() {
        Properties props = new Properties();
        try (InputStream in = TestApiConfig.class.getResourceAsStream("/config/config.properties")) {
            if (in == null) throw new IOException("config.properties not found");
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("config.properties load failed: " + e);
        }
        return props;
    }

    public String getHost() {
        String baseUrl = configProps.getProperty("ws.baseUrl");
        System.out.println("baseUrl: " + baseUrl);
        try {
            URI uri = new URI(baseUrl);
            System.out.println("baseUrl: " + baseUrl);
            return uri.getHost();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("ws.baseUrl 형식 오류: " + baseUrl, e);
        }
    }
    public String getProperty(String key) {
        return instanceProps.getProperty(key);
    }
}