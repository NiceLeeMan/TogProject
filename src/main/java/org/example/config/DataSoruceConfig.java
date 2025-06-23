package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * DataSoruceConfig 클래스
 * -----------------------
 * 애플리케이션 전역에서 사용할 HikariCP 기반의 DataSource(커넥션 풀)를
 * 설정하고 제공하는 팩토리 클래스입니다.
 *
 * 주요 역할:
 * 1. src/main/resources/config/db.properties 파일로부터
 *    JDBC URL, 사용자명, 비밀번호를 읽어온다.
 * 2. HikariConfig에 위 정보를 세팅하여 HikariCP 커넥션 풀을 초기화한다.
 * 3. 초기화된 HikariDataSource 객체를 반환하여 DAO나 서비스 레이어에서
 *    getConnection() 호출 시 동일한 풀을 재사용하도록 한다.
 *
 * 사용 방법:
 *    HikariDataSource ds = DataSoruceConfig.getDataSource();
 *    Connection conn = ds.getConnection();
 *
 * 장점:
 * - 설정 중복 제거: 여러 DAO/서비스에서 공통 설정 코드를 공유
 * - 환경별 분리: 프로퍼티 파일만 교체하여 개발/테스트/운영 설정 지원
 * - 성능 최적화: 커넥션 풀링으로 빈번한 연결 생성/해제 비용 절감
 * - 유지보수 편의: 풀 옵션 변경 시 해당 클래스만 수정
 */


public class DataSoruceConfig {

    public static HikariDataSource getDataSource() {
        Properties props = new Properties();
        try (InputStream in = DataSoruceConfig.class.getClassLoader().getResourceAsStream("config/db.properties")) {
            if (in == null) throw new RuntimeException(" db.properties 파일을 찾을 수 없습니다!");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("⚠ db.properties 로딩 실패", e);
        }

        String url = props.getProperty("jdbc.url");
        System.out.println("JDBC URL 로딩됨: " + url); // 디버깅용

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        System.out.println("url: " + url);
        config.setUsername(props.getProperty("jdbc.username"));
        config.setPassword(props.getProperty("jdbc.password"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        return new HikariDataSource(config);
    }

}
