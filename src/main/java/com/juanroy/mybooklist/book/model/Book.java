package com.juanroy.mybooklist.book.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String openLibraryId;

    private String title;

    private String authorName;

    private Long coverId;

    private String isbn;

    private String firstSentence;

    private String description;

    public Book(){};

    public Book(String title, String authorName, Long coverId, String isbn, String firstSentence, String description) {
        this.title = title;
        this.authorName = authorName;
        this.coverId = coverId;
        this.isbn = isbn;
        this.firstSentence = firstSentence;
        this.description = description;
    }
}

