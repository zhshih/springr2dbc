package com.example.springr2dbc.mapper;

import com.example.springr2dbc.dto.BookRequest;
import com.example.springr2dbc.dto.BookResponse;
import com.example.springr2dbc.model.Book;

public class BookMapper {

    public static Book toEntity(BookRequest dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setDescription(dto.getDescription());
        return book;
    }

    public static BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getDescription());
    }
}
