package com.example.springr2dbc.controller;

import com.example.springr2dbc.dto.BookRequest;
import com.example.springr2dbc.dto.BookResponse;
import com.example.springr2dbc.mapper.BookMapper;
import com.example.springr2dbc.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public Mono<ResponseEntity<Page<BookResponse>>> getAllBooks(Pageable pageable) {
        return bookService.getAllBooks(pageable)
                .map(page -> {
                    // Convert Page<Book> -> Page<BookResponse>
                    Page<BookResponse> responsePage =
                            page.map(BookMapper::toResponse);

                    return ResponseEntity.ok(responsePage);
                })
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<BookResponse>> getBookById(@PathVariable int id) {
        return bookService.getBookById(id)
                .map(BookMapper::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<BookResponse>> createBook(@RequestBody BookRequest request) {
        return bookService.saveBook(BookMapper.toEntity(request))
                .map(BookMapper::toResponse)
                .map(savedBook -> ResponseEntity
                        .created(URI.create("/api/books/" + savedBook.getId()))
                        .body(savedBook));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<BookResponse>> updateBook(@PathVariable int id, @RequestBody BookRequest request) {
        return bookService.updateBook(id, BookMapper.toEntity(request))
                .map(BookMapper::toResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBook(@PathVariable int id) {
        return bookService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAllBooks() {
        return bookService.deleteAll();
    }
}
