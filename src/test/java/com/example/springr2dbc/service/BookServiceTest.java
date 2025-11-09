package com.example.springr2dbc.service;


import com.example.springr2dbc.model.Book;
import com.example.springr2dbc.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        book1 = new Book(1, "Book A", "Desc A");
        book2 = new Book(2, "Book B", "Desc B");
    }

    @Test
    void shouldReturnAllBooks() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Book> books = List.of(book1, book2);
        long total = books.size();

        Mockito.when(bookRepository.findAllPaged(pageable.getPageSize(), (int) pageable.getOffset()))
                .thenReturn(Flux.fromIterable(books));
        Mockito.when(bookRepository.countAll())
                .thenReturn(Mono.just(total));

        Mono<Page<Book>> result = bookService.getAllBooks(pageable);

        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.getContent()).containsExactly(book1, book2);
                    assertThat(page.getTotalElements()).isEqualTo(total);
                    assertThat(page.getTotalPages()).isEqualTo(1);
                    assertThat(page.getNumber()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBookById() {
        Mockito.when(bookRepository.findById(1))
                .thenReturn(Mono.just(book1));

        StepVerifier.create(bookService.getBookById(1))
                .expectNext(book1)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenBookNotFound() {
        Mockito.when(bookRepository.findById(99))
                .thenReturn(Mono.empty());

        StepVerifier.create(bookService.getBookById(99))
                .verifyComplete();
    }

    @Test
    void shouldSaveBook() {
        Mockito.when(bookRepository.save(book1))
                .thenReturn(Mono.just(book1));

        StepVerifier.create(bookService.saveBook(book1))
                .expectNext(book1)
                .verifyComplete();
    }

    @Test
    void shouldUpdateExistingBook() {
        Book updated = new Book(1, "Updated", "Updated Desc");

        Mockito.when(bookRepository.findById(1))
                .thenReturn(Mono.just(book1));
        Mockito.when(bookRepository.save(Mockito.any(Book.class)))
                .thenReturn(Mono.just(updated));

        StepVerifier.create(bookService.updateBook(1, updated))
                .expectNext(updated)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenUpdatingNonExistingBook() {
        Book updated = new Book(1, "Updated", "Updated Desc");

        Mockito.when(bookRepository.findById(1))
                .thenReturn(Mono.empty());

        StepVerifier.create(bookService.updateBook(1, updated))
                .verifyComplete();
    }

    @Test
    void shouldDeleteBookById() {
        Mockito.when(bookRepository.deleteById(1))
                .thenReturn(Mono.empty());

        StepVerifier.create(bookService.deleteById(1))
                .verifyComplete();
    }

    @Test
    void shouldDeleteAllBooks() {
        Mockito.when(bookRepository.deleteAll())
                .thenReturn(Mono.empty());

        StepVerifier.create(bookService.deleteAll())
                .verifyComplete();
    }
}
