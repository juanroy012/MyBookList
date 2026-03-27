package com.juanroy.mybooklist.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseUrlInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConnectionSettings settings = resolve(applicationContext.getEnvironment());
        if (settings == null) {
            return;
        }

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put("spring.datasource.url", settings.url());

        if (StringUtils.hasText(settings.username())) {
            overrides.put("spring.datasource.username", settings.username());
        }
        if (StringUtils.hasText(settings.password())) {
            overrides.put("spring.datasource.password", settings.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource("databaseUrlOverrides", overrides));
    }

    static ConnectionSettings resolve(Environment environment) {
        String rawUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("spring.datasource.url")
        );

        String username = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                environment.getProperty("spring.datasource.username")
        );

        String password = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                environment.getProperty("spring.datasource.password")
        );

        return normalize(rawUrl, username, password);
    }

    static ConnectionSettings normalize(String rawUrl, String username, String password) {
        if (!StringUtils.hasText(rawUrl)) {
            return null;
        }

        if (rawUrl.startsWith("jdbc:")) {
            return new ConnectionSettings(rawUrl, username, password);
        }

        if (!rawUrl.startsWith("postgres://") && !rawUrl.startsWith("postgresql://")) {
            return new ConnectionSettings(rawUrl, username, password);
        }

        URI uri = URI.create(rawUrl);
        if (!StringUtils.hasText(uri.getHost()) || !StringUtils.hasText(uri.getPath()) || "/".equals(uri.getPath())) {
            throw new IllegalArgumentException("DATABASE_URL / SPRING_DATASOURCE_URL must include host and database name");
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());

        if (uri.getPort() != -1) {
            jdbcUrl.append(":").append(uri.getPort());
        }

        jdbcUrl.append(uri.getPath());

        if (StringUtils.hasText(uri.getQuery())) {
            jdbcUrl.append("?").append(uri.getQuery());
        }

        String resolvedUsername = username;
        String resolvedPassword = password;

        if (StringUtils.hasText(uri.getUserInfo())) {
            String[] parts = uri.getUserInfo().split(":", 2);
            if (!StringUtils.hasText(resolvedUsername) && parts.length > 0) {
                resolvedUsername = decode(parts[0]);
            }
            if (!StringUtils.hasText(resolvedPassword) && parts.length > 1) {
                resolvedPassword = decode(parts[1]);
            }
        }

        return new ConnectionSettings(jdbcUrl.toString(), resolvedUsername, resolvedPassword);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    record ConnectionSettings(String url, String username, String password) {
    }
}

