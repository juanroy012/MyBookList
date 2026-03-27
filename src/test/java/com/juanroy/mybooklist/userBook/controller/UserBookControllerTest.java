package com.juanroy.mybooklist.userBook.controller;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.juanroy.mybooklist.auth.service.JwtService;
import com.juanroy.mybooklist.book.model.Book;
import com.juanroy.mybooklist.user.model.User;
import com.juanroy.mybooklist.user.repository.UserRepository;
import com.juanroy.mybooklist.userBook.dto.UserBookDto;
import com.juanroy.mybooklist.userBook.model.ReadingStatus;
import com.juanroy.mybooklist.userBook.model.UserBook;
import com.juanroy.mybooklist.userBook.service.UserBookService;
import com.juanroy.mybooklist.config.ApplicationConfiguration;
import com.juanroy.mybooklist.config.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserBookController.class)
@Import({SecurityConfiguration.class, ApplicationConfiguration.class})
class UserBookControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean UserBookService userBookService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    private User testUser;
    private Book testBook;
    private UserBook testUserBook;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setEnabled(true);

        testBook = new Book("Test Book", "Test Author", 123L, "1234567890", "First sentence", "Description");
        testBook.setOpenLibraryId("OL123M");

        testUserBook = new UserBook(testUser, testBook);
        testUserBook.setReadingStatus(ReadingStatus.IN_PROGRESS);
        testUserBook.setRating(7);
        testUserBook.setReview("Good so far");
    }

    // ── GET /userCollection ─────────────────────────────────────────

    @Test
    void getCollection_withAuth_returnsListOfUserBooks() throws Exception {
        when(userBookService.getCollection(any(User.class))).thenReturn(List.of(testUserBook));

        mockMvc.perform(get("/userCollection").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].readingStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].rating").value(7))
                .andExpect(jsonPath("$[0].review").value("Good so far"))
                // user must be hidden via @JsonIgnore on UserBook.user
                .andExpect(jsonPath("$[0].user").doesNotExist());
    }

    @Test
    void getCollection_withAuth_emptyCollection_returnsEmptyArray() throws Exception {
        when(userBookService.getCollection(any(User.class))).thenReturn(List.of());

        mockMvc.perform(get("/userCollection").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCollection_withoutAuth_returns4xx() throws Exception {
        mockMvc.perform(get("/userCollection"))
                .andExpect(status().is4xxClientError());
    }

    // ── POST /userCollection ────────────────────────────────────────

    @Test
    void addCollection_withValidBody_returns200AndSavedEntry() throws Exception {
        when(userBookService.addCollection(any(UserBookDto.class), any(User.class)))
                .thenReturn(testUserBook);

        UserBookDto dto = buildDto(testBook, ReadingStatus.IN_PROGRESS, 7, "Good so far");

        mockMvc.perform(post("/userCollection")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readingStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.rating").value(7));
    }

    @Test
    void addCollection_withDuplicateBook_returns4xx() throws Exception {
        when(userBookService.addCollection(any(), any()))
                .thenThrow(new RuntimeException("Book already in collection"));

        UserBookDto dto = buildDto(testBook, ReadingStatus.COMPLETED, 9, "Great");

        mockMvc.perform(post("/userCollection")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void addCollection_withoutAuth_returns4xx() throws Exception {
        UserBookDto dto = buildDto(testBook, ReadingStatus.COMPLETED, 9, "Great");

        mockMvc.perform(post("/userCollection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    // ── PATCH /userCollection/{openLibraryId} ───────────────────────

    @Test
    void editCollection_withValidBody_returns200AndUpdatedEntry() throws Exception {
        UserBook updated = new UserBook(testUser, testBook);
        updated.setReadingStatus(ReadingStatus.COMPLETED);
        updated.setRating(9);
        updated.setReview("Amazing!");

        when(userBookService.editCollection(eq("OL123M"), any(UserBookDto.class), any(User.class)))
                .thenReturn(updated);

        // For PATCH only editable fields are needed in the body; book comes from path
        String body = """
                {
                  "readingStatus": "COMPLETED",
                  "rating": 9,
                  "review": "Amazing!"
                }
                """;

        mockMvc.perform(patch("/userCollection/OL123M")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.rating").value(9))
                .andExpect(jsonPath("$.review").value("Amazing!"));
    }

    @Test
    void editCollection_withNonExistentBook_returns4xx() throws Exception {
        when(userBookService.editCollection(eq("INVALID"), any(), any()))
                .thenThrow(new RuntimeException("Book not found in database"));

        String body = """
                { "readingStatus": "COMPLETED", "rating": 8, "review": "ok" }
                """;

        mockMvc.perform(patch("/userCollection/INVALID")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void editCollection_withBookNotInCollection_returns4xx() throws Exception {
        when(userBookService.editCollection(eq("OL123M"), any(), any()))
                .thenThrow(new RuntimeException("Book not in your collection"));

        String body = """
                { "readingStatus": "DROPPED", "rating": 3, "review": "nope" }
                """;

        mockMvc.perform(patch("/userCollection/OL123M")
                        .with(user(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void editCollection_withoutAuth_returns4xx() throws Exception {
        mockMvc.perform(patch("/userCollection/OL123M")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    // ── DELETE /userCollection/{openLibraryId} ──────────────────────

    @Test
    void removeCollection_withValidBook_returns200WithMessage() throws Exception {
        doNothing().when(userBookService).removeCollection(eq("OL123M"), any(User.class));

        mockMvc.perform(delete("/userCollection/OL123M").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("Removed from collection"));
    }

    @Test
    void removeCollection_withNonExistentBook_returns4xx() throws Exception {
        doThrow(new RuntimeException("Book not found in database"))
                .when(userBookService).removeCollection(eq("INVALID"), any());

        mockMvc.perform(delete("/userCollection/INVALID").with(user(testUser)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void removeCollection_withBookNotInCollection_returns4xx() throws Exception {
        doThrow(new RuntimeException("Book not in your collection"))
                .when(userBookService).removeCollection(eq("OL123M"), any());

        mockMvc.perform(delete("/userCollection/OL123M").with(user(testUser)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void removeCollection_withoutAuth_returns4xx() throws Exception {
        mockMvc.perform(delete("/userCollection/OL123M"))
                .andExpect(status().is4xxClientError());
    }

    // ── helpers ─────────────────────────────────────────────────────

    private UserBookDto buildDto(Book book, ReadingStatus status, int rating, String review) {
        UserBookDto dto = new UserBookDto();
        dto.setBook(book);
        dto.setReadingStatus(status);
        dto.setRating(rating);
        dto.setReview(review);
        return dto;
    }
}

