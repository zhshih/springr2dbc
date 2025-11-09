package com.example.springr2dbc.controller;


import com.example.springr2dbc.config.WebConfig;
import com.example.springr2dbc.dto.BookRequest;
import com.example.springr2dbc.dto.BookResponse;
import com.example.springr2dbc.model.Book;
import com.example.springr2dbc.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

@WebFluxTest(BookController.class)
@ContextConfiguration(classes = BookController.class)
@Import(WebConfig.class)
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    private Book book1;
    private Book book2;
    private BookResponse response1;

    @BeforeEach
    void setUp() {
        book1 = new Book(1, "Book A", "Desc A");
        book2 = new Book(2, "Book B", "Desc B");

        response1 = new BookResponse(1, "Book A", "Desc A");
    }

    @Test
    void shouldReturnAllBooks() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Book> books = List.of(book1, book2);
        Page<Book> page = new PageImpl<>(books, pageable, books.size());

        Mockito.when(bookService.getAllBooks(pageable))
                .thenReturn(Mono.just(page));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/books")
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].title").isEqualTo(book1.getTitle())
                .jsonPath("$.content[1].title").isEqualTo(book2.getTitle());
    }

    @Test
    void shouldReturnNoContentWhenNoBooks() {
        Pageable pageable = PageRequest.of(0, 20);

        Mockito.when(bookService.getAllBooks(pageable))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/books")
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldReturnBookByIdIfExists() {
        Mockito.when(bookService.getBookById(1))
                .thenReturn(Mono.just(book1));

        webTestClient.get()
                .uri("/api/v1/books/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookResponse.class)
                .isEqualTo(response1);
    }

    @Test
    void shouldReturn404IfBookNotFound() {
        Mockito.when(bookService.getBookById(1))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/books/{id}", 1)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldCreateBook() {
        BookRequest request = new BookRequest("Book A", "Desc A");
        Mockito.when(bookService.saveBook(Mockito.any(Book.class)))
                .thenReturn(Mono.just(book1));

        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/books/1")
                .expectBody(BookResponse.class)
                .isEqualTo(response1);
    }

    @Test
    void shouldUpdateBookIfExists() {
        BookRequest request = new BookRequest("Updated", "Updated Desc");
        Book updatedBook = new Book(1, "Updated", "Updated Desc");
        BookResponse updatedResponse = new BookResponse(1, "Updated", "Updated Desc");

        Mockito.when(bookService.updateBook(Mockito.eq(1), Mockito.any(Book.class)))
                .thenReturn(Mono.just(updatedBook));

        webTestClient.put()
                .uri("/api/v1/books/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookResponse.class)
                .isEqualTo(updatedResponse);
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingBook() {
        BookRequest request = new BookRequest("Updated", "Updated Desc");

        Mockito.when(bookService.updateBook(Mockito.eq(1), Mockito.any(Book.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/v1/books/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteBookIfExists() {
        Mockito.when(bookService.deleteById(1))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/books/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldDeleteAllBooks() {
        Mockito.when(bookService.deleteAll())
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/books")
                .exchange()
                .expectStatus().isNoContent();
    }
}
