package com.juanroy.mybooklist.userBook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_books")
@Getter
@Setter
public class UserBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToOne
    private Book book;

    @Enumerated(EnumType.STRING)
    private ReadingStatus readingStatus;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Integer rating;

    private String review;

    public UserBook() {};

    public UserBook(User user, Book book) {
        this.user = user;
        this.book = book;
    }
}
