package com.juanroy.mybooklist.userBook.controller;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.book.repository.BookRepository;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.dto.UserBookDto;
import com.juanroy.mybooklist.userBook.model.UserBook;
import com.juanroy.mybooklist.userBook.repository.UserBookRepository;
import com.juanroy.mybooklist.userBook.service.UserBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/userCollection")
public class UserBookController {

    private final UserBookService userBookService;

    public UserBookController(UserBookService userBookService) {
        this.userBookService = userBookService;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<UserBook>> getCollection() {
        List<UserBook> books = userBookService.getCollection(getCurrentUser());
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<UserBook> addCollection(@RequestBody UserBookDto userBookDto) {
        UserBook userBook = userBookService.addCollection(userBookDto, getCurrentUser());
        return ResponseEntity.ok(userBook);
    }

    @PatchMapping("/{openLibraryId}")
    public ResponseEntity<UserBook> editCollection(
            @PathVariable String openLibraryId,
            @RequestBody UserBookDto userBookDto) {
        UserBook userBook = userBookService.editCollection(openLibraryId, userBookDto, getCurrentUser());
        return ResponseEntity.ok(userBook);
    }

    @DeleteMapping("/{openLibraryId}")
    public ResponseEntity<String> removeCollection(@PathVariable String openLibraryId) {
        userBookService.removeCollection(openLibraryId, getCurrentUser());
        return ResponseEntity.ok("Removed from collection");
    }
}
