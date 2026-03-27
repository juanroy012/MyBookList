package com.juanroy.mybooklist.userBook.repository;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.model.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findUserBooksByUser(User user);

    Optional<UserBook> findUserBookByUserAndBook(User user, Book book);
}
