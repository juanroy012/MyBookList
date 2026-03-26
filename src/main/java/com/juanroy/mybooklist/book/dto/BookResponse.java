package com.juanroy.mybooklist.book.dto;

public record BookResponse(
        String openLibraryId,
        String title,
        String authorName,
        Long coverId,
        String isbn,
        String firstSentence,
        String description
) {
}
