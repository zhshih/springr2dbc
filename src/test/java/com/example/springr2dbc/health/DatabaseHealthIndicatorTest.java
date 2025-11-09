package com.example.springr2dbc.health;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class DatabaseHealthIndicatorTest {

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        connectionFactory = mock(ConnectionFactory.class);
        connection = mock(Connection.class);
        healthIndicator = new DatabaseHealthIndicator(connectionFactory);
    }

    @Test
    void healthShouldBeUpWhenConnectionSucceeds() {
        when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
        when(connection.close()).thenAnswer(invocation -> Mono.empty());

        Mono<Health> healthMono = healthIndicator.health();

        StepVerifier.create(healthMono)
                .expectNextMatches(health -> health.getStatus().equals(Health.up().build().getStatus()))
                .verifyComplete();

        verify(connectionFactory).create();
        verify(connection).close();
    }

    @Test
    void healthShouldBeDownWhenConnectionFails() {
        RuntimeException exception = new RuntimeException("Connection failed");
        when(connectionFactory.create()).thenReturn(Mono.error(exception));

        Mono<Health> healthMono = healthIndicator.health();

        StepVerifier.create(healthMono)
                .expectNextMatches(health -> {
                    if (!health.getStatus().equals(Health.down(exception).build().getStatus())) {
                        return false;
                    }
                    Object errorObj = health.getDetails().get("error");
                    return switch (errorObj) {
                        case null -> false;
                        case Throwable error -> error.getClass().equals(exception.getClass())
                                && error.getMessage().equals(exception.getMessage());
                        case String errorStr -> errorStr.contains(exception.getClass().getName())
                                && errorStr.contains(exception.getMessage());
                        default -> false;
                    };
                })
                .verifyComplete();
    }

    @Test
    void healthShouldBeDownWhenCloseFails() {
        when(connectionFactory.create()).thenAnswer(invocation -> Mono.just(connection));
        RuntimeException exception = new RuntimeException("Close failed");
        when(connection.close()).thenReturn(Mono.error(exception));

        Mono<Health> healthMono = healthIndicator.health();

        StepVerifier.create(healthMono)
                .expectNextMatches(health -> {
                    if (!health.getStatus().equals(Health.down(exception).build().getStatus())) {
                        return false;
                    }
                    Object errorObj = health.getDetails().get("error");
                    return switch (errorObj) {
                        case Throwable error -> error.getClass().equals(exception.getClass())
                                && error.getMessage().equals(exception.getMessage());
                        case String errorStr -> errorStr.contains(exception.getClass().getName())
                                && errorStr.contains(exception.getMessage());
                        case null, default -> false;
                    };

                })
                .verifyComplete();
    }
}
