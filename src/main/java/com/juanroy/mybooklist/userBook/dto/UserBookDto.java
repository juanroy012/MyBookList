package com.juanroy.mybooklist.userBook.dto;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.model.ReadingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBookDto {
    private User user;
    private Book book;
    private ReadingStatus readingStatus;
    @Min(value = 1, message = "Rating cannot be below 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private int rating;
    private String review;
}
