package com.juanroy.mybooklist.userBook.model;

import lombok.Getter;

@Getter
public enum ReadingStatus {
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    PLAN_TO_READ(" Plan to Read"),
    DROPPED("Dropped");

    private final String label;

    ReadingStatus(String label) {
        this.label = label;
    }
}
