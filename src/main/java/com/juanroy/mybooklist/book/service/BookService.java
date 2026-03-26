package com.juanroy.mybooklist.book.service;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.book.repository.BookRepository;

import java.util.List;

public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAll() {
        return bookRepository.findAll();
    }
}
