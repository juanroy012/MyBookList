package com.juanroy.mybooklist.userBook.service;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.book.repository.BookRepository;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.dto.UserBookDto;
import com.juanroy.mybooklist.userBook.model.UserBook;
import com.juanroy.mybooklist.userBook.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBookService {

    private final UserBookRepository userBookRepository;
    private final BookRepository bookRepository;

    public UserBookService(UserBookRepository userBookRepository, BookRepository bookRepository) {
        this.userBookRepository = userBookRepository;
        this.bookRepository = bookRepository;
    }

    public List<UserBook> getCollection(User user) {
        return userBookRepository.findUserBooksByUser(user);
    }

    public UserBook addCollection(UserBookDto userBookDto, User user) {
        Book book = bookRepository.findBookByOpenLibraryId(userBookDto.getBook().getOpenLibraryId())
                .orElseGet(() -> bookRepository.save(userBookDto.getBook()));

        userBookRepository.findUserBookByUserAndBook(user, book).ifPresent((existing) -> {
                throw new RuntimeException("Book already in collection");}
        );

        UserBook userBook = new UserBook(user, book);
        userBook.setReadingStatus(userBookDto.getReadingStatus());
        userBook.setReview(userBookDto.getReview());
        userBook.setRating(userBookDto.getRating());
        return userBookRepository.save(userBook);
    }

    public UserBook editCollection(String openLibraryId, UserBookDto userBookDto, User user) {
        Book book = bookRepository.findBookByOpenLibraryId(openLibraryId)
                .orElseThrow(() -> new RuntimeException("Book not found in database"));

        UserBook existing = userBookRepository.findUserBookByUserAndBook(user, book)
                .orElseThrow(() -> new RuntimeException("Book not in your collection"));

        existing.setReadingStatus(userBookDto.getReadingStatus());
        existing.setRating(userBookDto.getRating());
        existing.setReview(userBookDto.getReview());
        return userBookRepository.save(existing);
    }

    public void removeCollection(String openLibraryId, User user) {
        Book book = bookRepository.findBookByOpenLibraryId(openLibraryId)
                .orElseThrow(() -> new RuntimeException("Book not found in database"));

        UserBook existing = userBookRepository.findUserBookByUserAndBook(user, book)
                .orElseThrow(() -> new RuntimeException("Book not in your collection"));

        userBookRepository.delete(existing);
    }
}
