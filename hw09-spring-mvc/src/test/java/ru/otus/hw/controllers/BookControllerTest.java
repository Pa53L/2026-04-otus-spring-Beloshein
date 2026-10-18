package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookController.class)
@DisplayName("Контроллер книг")
class BookControllerTest {

    private static final long BOOK_ID = 7L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private BookCommentService commentService;

    @Test
    @DisplayName("Главная страница перенаправляет на список книг")
    void shouldRedirectHomeToBooks() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verifyNoInteractions(bookService, authorService, genreService, commentService);
    }

    @Test
    @DisplayName("Список показывает книги")
    void shouldShowBookList() throws Exception {
        var books = List.of(sampleBook());
        given(bookService.findAll()).willReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("books", books))
                .andExpect(content().string(containsString("Spring in Action")));

        verify(bookService).findAll();
    }

    @Test
    @DisplayName("Страница книги показывает её данные и комментарии")
    void shouldShowBookDetailsWithComments() throws Exception {
        var book = sampleBook();
        var comments = List.of(new BookComment("Helpful book", book));
        given(bookService.findById(BOOK_ID)).willReturn(Optional.of(book));
        given(commentService.findByBookId(BOOK_ID)).willReturn(comments);

        mockMvc.perform(get("/books/{id}", BOOK_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("comments", comments))
                .andExpect(content().string(containsString("Helpful book")));

        verify(bookService).findById(BOOK_ID);
        verify(commentService).findByBookId(BOOK_ID);
    }

    @Test
    @DisplayName("Форма создания содержит списки авторов и жанров")
    void shouldShowCreateForm() throws Exception {
        var book = sampleBook();
        var authors = List.of(book.getAuthor());
        var genres = book.getGenres();
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);

        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/create"))
                .andExpect(model().attributeExists("bookForm"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));
    }

    @Test
    @DisplayName("Создание сохраняет книгу и перенаправляет на список")
    void shouldCreateBook() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "New book")
                        .param("authorId", "3")
                        .param("genreIds", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).insert("New book", 3L, Set.of(2L));
    }

    @Test
    @DisplayName("Форма редактирования заполнена данными книги")
    void shouldShowEditForm() throws Exception {
        var book = sampleBook();
        var authors = List.of(book.getAuthor());
        var genres = book.getGenres();
        given(bookService.findById(BOOK_ID)).willReturn(Optional.of(book));
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);

        var result = mockMvc.perform(get("/books/{id}/edit", BOOK_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().attribute("bookId", BOOK_ID))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres))
                .andReturn();

        var form = (BookForm) result.getModelAndView().getModel().get("bookForm");
        assertThat(form.getTitle()).isEqualTo("Spring in Action");
        assertThat(form.getAuthorId()).isEqualTo(3L);
        assertThat(form.getGenreIds()).containsExactly(2L);
    }

    @Test
    @DisplayName("Обновление сохраняет изменения и перенаправляет на список")
    void shouldUpdateBook() throws Exception {
        mockMvc.perform(post("/books/{id}", BOOK_ID)
                        .param("title", "Updated book")
                        .param("authorId", "3")
                        .param("genreIds", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).update(BOOK_ID, "Updated book", 3L, Set.of(2L));
    }

    @Test
    @DisplayName("Удаление использует POST и перенаправляет на список")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(post("/books/{id}/delete", BOOK_ID))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(BOOK_ID);
    }

    @Test
    @DisplayName("При ошибке обновления форма открывается повторно с ошибками")
    void shouldShowEditFormWhenUpdateIsInvalid() throws Exception {
        var book = sampleBook();
        var authors = List.of(book.getAuthor());
        var genres = book.getGenres();
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);

        mockMvc.perform(post("/books/{id}", BOOK_ID)
                        .param("title", " ")
                        .param("authorId", "3")
                        .param("genreIds", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title"))
                .andExpect(model().attribute("bookId", BOOK_ID))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres))
                .andExpect(content().string(containsString("action=\"/books/7\"")))
                .andExpect(content().string(containsString("Введите название книги")));

        verifyNoInteractions(bookService);
    }

    private Book sampleBook() {
        var author = new Author(3L, "Craig Walls");
        var genre = new Genre(2L, "Technology");
        return new Book(BOOK_ID, "Spring in Action", author, List.of(genre));
    }
}
