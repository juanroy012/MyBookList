package com.juanroy.mybooklist.userBook.service;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.dto.UserBookDto;
import com.juanroy.mybooklist.userBook.model.UserBook;
import com.juanroy.mybooklist.userBook.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBookService {

    private final UserBookRepository userBookRepository;

    public UserBookService(UserBookRepository userBookRepository) {
        this.userBookRepository = userBookRepository;
    }

    public List<Book> getCollection(User user) {
        List<UserBook> collection = userBookRepository.findUserBooksByUser(user);
        return collection.stream()
                .map((userBook) -> userBook.getBook())
                .toList();
    }

    public UserBook addCollection(UserBookDto userBookDto) {
        UserBook collection = new UserBook(userBookDto.getUser(), userBookDto.getBook());
        collection.setReadingStatus(userBookDto.getReadingStatus());
        collection.setRating(userBookDto.getRating());
        collection.setReview(userBookDto.getReview());
        return userBookRepository.save(collection);
    }
}
