package com.juanroy.mybooklist.userBook.service;

import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.book.repository.BookRepository;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.userBook.dto.UserBookDto;
import com.juanroy.mybooklist.userBook.model.ReadingStatus;
import com.juanroy.mybooklist.userBook.model.UserBook;
import com.juanroy.mybooklist.userBook.repository.UserBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBookServiceTest {

    @Mock UserBookRepository userBookRepository;
    @Mock BookRepository bookRepository;
    @InjectMocks UserBookService userBookService;

    private User testUser;
    private Book testBook;
    private UserBook testUserBook;
    private UserBookDto dto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setEnabled(true);

        testBook = new Book("Test Book", "Test Author", 123L, "1234567890", "First sentence", "Description");
        testBook.setOpenLibraryId("OL123M");

        testUserBook = new UserBook(testUser, testBook);
        testUserBook.setReadingStatus(ReadingStatus.IN_PROGRESS);
        testUserBook.setRating(7);
        testUserBook.setReview("Good so far");

        dto = new UserBookDto();
        dto.setBook(testBook);
        dto.setReadingStatus(ReadingStatus.COMPLETED);
        dto.setRating(9);
        dto.setReview("Amazing!");
    }

    // ── getCollection ───────────────────────────────────────────────

    @Test
    void getCollection_returnsAllBooksForUser() {
        when(userBookRepository.findUserBooksByUser(testUser)).thenReturn(List.of(testUserBook));

        List<UserBook> result = userBookService.getCollection(testUser);

        assertThat(result).hasSize(1).containsExactly(testUserBook);
    }

    @Test
    void getCollection_withNoBooks_returnsEmptyList() {
        when(userBookRepository.findUserBooksByUser(testUser)).thenReturn(List.of());

        assertThat(userBookService.getCollection(testUser)).isEmpty();
    }

    // ── addCollection ───────────────────────────────────────────────

    @Test
    void addCollection_withNewBook_savesBookThenUserBook() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.empty());
        when(bookRepository.save(testBook)).thenReturn(testBook);
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.empty());
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBook result = userBookService.addCollection(dto, testUser);

        verify(bookRepository).save(testBook);
        assertThat(result.getBook()).isEqualTo(testBook);
        assertThat(result.getReadingStatus()).isEqualTo(ReadingStatus.COMPLETED);
        assertThat(result.getRating()).isEqualTo(9);
        assertThat(result.getReview()).isEqualTo("Amazing!");
    }

    @Test
    void addCollection_withAlreadyStoredBook_reusesExistingAndDoesNotSaveAgain() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.of(testBook));
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.empty());
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(inv -> inv.getArgument(0));

        userBookService.addCollection(dto, testUser);

        verify(bookRepository, never()).save(any(Book.class));
        verify(userBookRepository).save(any(UserBook.class));
    }

    @Test
    void addCollection_withBookAlreadyInCollection_throwsException() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.of(testBook));
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.of(testUserBook));

        assertThatThrownBy(() -> userBookService.addCollection(dto, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already in collection");

        verify(userBookRepository, never()).save(any());
    }

    // ── editCollection ──────────────────────────────────────────────

    @Test
    void editCollection_updatesReadingStatusRatingAndReview() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.of(testBook));
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.of(testUserBook));
        when(userBookRepository.save(testUserBook)).thenReturn(testUserBook);

        UserBook result = userBookService.editCollection("OL123M", dto, testUser);

        assertThat(result.getReadingStatus()).isEqualTo(ReadingStatus.COMPLETED);
        assertThat(result.getRating()).isEqualTo(9);
        assertThat(result.getReview()).isEqualTo("Amazing!");
        verify(userBookRepository).save(testUserBook);
    }

    @Test
    void editCollection_withNonExistentBook_throwsException() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBookService.editCollection("OL123M", dto, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void editCollection_withBookNotInUsersCollection_throwsException() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.of(testBook));
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBookService.editCollection("OL123M", dto, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not in your collection");
    }

    // ── removeCollection ────────────────────────────────────────────

    @Test
    void removeCollection_withValidBook_deletesUserBook() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.of(testBook));
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.of(testUserBook));

        userBookService.removeCollection("OL123M", testUser);

        verify(userBookRepository).delete(testUserBook);
    }

    @Test
    void removeCollection_withNonExistentBook_throwsException() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBookService.removeCollection("OL123M", testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(userBookRepository, never()).delete(any());
    }

    @Test
    void removeCollection_withBookNotInUsersCollection_throwsException() {
        when(bookRepository.findBookByOpenLibraryId("OL123M")).thenReturn(Optional.of(testBook));
        when(userBookRepository.findUserBookByUserAndBook(testUser, testBook)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userBookService.removeCollection("OL123M", testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not in your collection");

        verify(userBookRepository, never()).delete(any());
    }
}

