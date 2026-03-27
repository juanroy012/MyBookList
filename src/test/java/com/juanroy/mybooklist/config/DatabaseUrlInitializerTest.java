package com.juanroy.mybooklist.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseUrlInitializerTest {

    @Test
    void normalize_keepsJdbcUrlUntouched() {
        DatabaseUrlInitializer.ConnectionSettings settings = DatabaseUrlInitializer.normalize(
                "jdbc:h2:mem:testdb",
                "sa",
                ""
        );

        assertNotNull(settings);
        assertEquals("jdbc:h2:mem:testdb", settings.url());
        assertEquals("sa", settings.username());
        assertEquals("", settings.password());
    }

    @Test
    void normalize_convertsFlyStylePostgresUrlToJdbcAndParsesCredentials() {
        DatabaseUrlInitializer.ConnectionSettings settings = DatabaseUrlInitializer.normalize(
                "postgres://dbuser:dbpass@db.example.internal:5432/mydb?sslmode=require&prepareThreshold=0",
                null,
                null
        );

        assertNotNull(settings);
        assertEquals(
                "jdbc:postgresql://db.example.internal:5432/mydb?sslmode=require&prepareThreshold=0",
                settings.url()
        );
        assertEquals("dbuser", settings.username());
        assertEquals("dbpass", settings.password());
    }

    @Test
    void normalize_prefersExplicitCredentialsOverUriCredentials() {
        DatabaseUrlInitializer.ConnectionSettings settings = DatabaseUrlInitializer.normalize(
                "postgres://dbuser:dbpass@db.example.internal:5432/mydb",
                "explicit-user",
                "explicit-pass"
        );

        assertNotNull(settings);
        assertEquals("jdbc:postgresql://db.example.internal:5432/mydb", settings.url());
        assertEquals("explicit-user", settings.username());
        assertEquals("explicit-pass", settings.password());
    }

    @Test
    void resolve_fallsBackToDatabaseUrlWhenSpringDatasourceUrlIsAbsent() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://dbuser:dbpass@db.example.internal:5432/mydb?sslmode=require");

        DatabaseUrlInitializer.ConnectionSettings settings = DatabaseUrlInitializer.resolve(environment);

        assertNotNull(settings);
        assertEquals("jdbc:postgresql://db.example.internal:5432/mydb?sslmode=require", settings.url());
        assertEquals("dbuser", settings.username());
        assertEquals("dbpass", settings.password());
    }
}

