package com.juanroy.mybooklist.userBook.controller;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.model.UserBook;
import com.juanroy.mybooklist.userBook.repository.UserBookRepository;
import com.juanroy.mybooklist.userBook.service.UserBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userCollection")
public class UserBookController {

    private final UserBookService userBookService;
    private final UserBookRepository userBookRepository;

    public UserBookController(UserBookService userBookService, UserBookRepository userBookRepository) {
        this.userBookService = userBookService;
        this.userBookRepository = userBookRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Book>> getCollection(@RequestBody User user) {
        List<Book> books = userBookService.getCollection(user);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/")
    public ResponseEntity<UserBook> addCollection(@RequestBody Book book) {
        UserBook userBook = new UserBook(book,)
    }
}
