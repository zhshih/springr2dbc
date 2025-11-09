package com.example.springr2dbc.service;

import com.example.springr2dbc.model.Book;
import com.example.springr2dbc.repository.BookRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;
    private final MeterRegistry meterRegistry;

    @Autowired
    public BookService(BookRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    @Retry(name = "bookService")
    @TimeLimiter(name = "bookService", fallbackMethod = "fallbackGetAllBooks")
    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackGetAllBooks")
    public Mono<Page<Book>> getAllBooks(Pageable pageable) {
        int limit = pageable.getPageSize();
        int offset = pageable.getPageNumber() * limit;

        Timer.Sample sample = Timer.start(meterRegistry);
        Mono<Long> totalCount = repository.countAll();
        Flux<Book> books = repository.findAllPaged(limit, offset);

        return books.collectList()
                .zipWith(totalCount, (content, total) ->
                        (Page<Book>) new PageImpl<>(content, pageable, total)
                )
                .doOnSubscribe(s -> meterRegistry.counter("book.getAllBooks.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.getAllBooks.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.getAllBooks.duration")));
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackFindByTitleContains")
    public Flux<Book> findByTitleContains(String title) {
        Timer.Sample sample = Timer.start(meterRegistry);

        return repository.findByTitleContains(title)
                .doOnSubscribe(s -> meterRegistry.counter("book.findByTitle.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.findByTitle.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.findByTitle.duration")));
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackGetBookById")
    public Mono<Book> getBookById(int id) {
        Timer.Sample sample = Timer.start(meterRegistry);

        return repository.findById(id)
                .doOnSubscribe(s -> meterRegistry.counter("book.getBookById.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.getBookById.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.getBookById.duration")));
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackSaveBook")
    public Mono<Book> saveBook(Book book) {
        Timer.Sample sample = Timer.start(meterRegistry);

        return repository.save(book)
                .doOnSubscribe(s -> meterRegistry.counter("book.saveBook.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.saveBook.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.saveBook.duration")));
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackUpdateBook")
    public Mono<Book> updateBook(int id, Book book) {
        Timer.Sample sample = Timer.start(meterRegistry);

        return repository.findById(id)
                .flatMap(existingBook -> {
                    existingBook.setTitle(book.getTitle());
                    existingBook.setDescription(book.getDescription());
                    return repository.save(existingBook);
                })
                .doOnSubscribe(s -> meterRegistry.counter("book.updateBook.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.updateBook.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.updateBook.duration")));
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackDeleteById")
    public Mono<Void> deleteById(int id) {
        Timer.Sample sample = Timer.start(meterRegistry);

        return repository.deleteById(id)
                .doOnSubscribe(s -> meterRegistry.counter("book.deleteById.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.deleteById.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.deleteById.duration")));
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackDeleteAll")
    public Mono<Void> deleteAll() {
        Timer.Sample sample = Timer.start(meterRegistry);

        return repository.deleteAll()
                .doOnSubscribe(s -> meterRegistry.counter("book.deleteAll.calls").increment())
                .doOnError(e -> meterRegistry.counter("book.deleteAll.errors").increment())
                .doFinally(signal -> sample.stop(meterRegistry.timer("book.deleteAll.duration")));
    }

    private Flux<Book> fallbackGetAllBooks(Throwable t) {
        return Flux.empty(); // return empty Flux on failure
    }

    private Flux<Book> fallbackFindByTitleContains(String title, Throwable t) {
        return Flux.empty();
    }

    private Mono<Book> fallbackGetBookById(int id, Throwable t) {
        return Mono.empty();
    }

    private Mono<Book> fallbackSaveBook(Book book, Throwable t) {
        return Mono.empty();
    }

    private Mono<Book> fallbackUpdateBook(int id, Book book, Throwable t) {
        return Mono.empty();
    }

    private Mono<Void> fallbackDeleteById(int id, Throwable t) {
        return Mono.empty();
    }

    private Mono<Void> fallbackDeleteAll(Throwable t) {
        return Mono.empty();
    }
}
