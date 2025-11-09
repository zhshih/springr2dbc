package com.example.springr2dbc.repository;

import com.example.springr2dbc.model.Book;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BookRepository extends R2dbcRepository<Book, Integer> {
    Mono<Book> findByDescription(String description);
    Flux<Book> findByTitleContains(String title);

    @Query("SELECT * FROM books LIMIT :limit OFFSET :offset")
    Flux<Book> findAllPaged(int limit, int offset);

    @Query("SELECT COUNT(*) FROM books")
    Mono<Long> countAll();
}
