package com.example.springr2dbc.health;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatabaseHealthIndicator implements ReactiveHealthIndicator {

    private final ConnectionFactory connectionFactory;

    public DatabaseHealthIndicator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<Health> health() {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.close())
                        .then(Mono.just(Health.up().build()))
                )
                .onErrorResume(ex -> Mono.just(Health.down(ex).build()));
    }
}
