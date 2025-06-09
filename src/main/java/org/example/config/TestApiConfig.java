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
    private final Properties instanceProps;

    static {
        try (InputStream in = TestApiConfig.class.getResourceAsStream("/api.properties")) {
            if (in == null) throw new IOException("api.properties not found");
            apiProps.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("api.properties 로드 실패: " + e);
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

    private static Properties loadConfigProperties() {
        Properties props = new Properties();
        try (InputStream in = TestApiConfig.class.getResourceAsStream("/config.properties")) {
            if (in == null) throw new IOException("config.properties not found");
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("config.properties load failed: " + e);
        }
        return props;
    }

    public String getHost() {
        String baseUrl = apiProps.getProperty("api.baseUrl");
        try {
            URI uri = new URI(baseUrl);
            return uri.getHost();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("api.baseUrl 형식 오류: " + baseUrl, e);
        }
    }
}