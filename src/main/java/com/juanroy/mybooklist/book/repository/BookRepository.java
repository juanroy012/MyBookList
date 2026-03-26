package com.juanroy.mybooklist.book.repository;

import com.juanroy.mybooklist.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
