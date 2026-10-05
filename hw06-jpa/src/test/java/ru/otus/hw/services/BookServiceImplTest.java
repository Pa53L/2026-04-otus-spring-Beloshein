package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({BookServiceImpl.class, JpaBookRepository.class, JpaAuthorRepository.class,
        JpaGenreRepository.class, BookConverter.class, AuthorConverter.class, GenreConverter.class})
@Transactional(propagation = Propagation.NEVER)
@Sql(scripts = {"/reset-data.sql", "/test-data.sql"})
@DisplayName("Сервис для работы с книгами")
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookConverter bookConverter;

    @Autowired
    private JdbcTemplate jdbc;

    @DisplayName("Должен загружать книги с доступными вне транзакции авторами и жанрами")
    @Test
    void shouldFindAllBooksWithoutLazyException() {
        var books = bookService.findAll();
        assertThat(books).extracting(Book::getTitle)
                .containsExactly("BookTitle_1", "BookTitle_2", "BookTitle_3");
        assertBook(books.get(0), "BookTitle_1", "Author_1", "Genre_1", "Genre_2");
        assertBook(books.get(1), "BookTitle_2", "Author_2", "Genre_3", "Genre_4");
        assertBook(books.get(2), "BookTitle_3", "Author_3", "Genre_5", "Genre_6");
    }

    @DisplayName("Должен сохранять книгу с доступными вне транзакции связями")
    @Test
    void shouldInsertNewBookWithoutLazyException() {
        var book = bookService.insert("newBook", 1L, Set.of(1L, 2L));
        assertThat(book.getId()).isPositive();
        assertBook(book, "newBook", "Author_1", "Genre_1", "Genre_2");
        assertBook(bookService.findById(book.getId()).orElseThrow(),
                "newBook", "Author_1", "Genre_1", "Genre_2");
    }

    @DisplayName("Должен удалять книгу и зависимые записи, сохраняя авторов и жанры")
    @Test
    void shouldDeleteBookAndDependentRowsButKeepReferenceData() {
        bookService.deleteById(1L);
        assertThat(bookService.findById(1L)).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from comments where book_id = 1", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from books_genres where book_id = 1", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from authors", Long.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from genres", Long.class)).isEqualTo(6);
        assertThat(bookService.findAll()).hasSize(2);
    }

    @DisplayName("Должен отклонять отсутствующие жанры без изменения книги")
    @Test
    void shouldRejectPartiallyMissingGenresWithoutChangingBook() {
        assertThatThrownBy(() -> bookService.update(1L, "invalid", 2L, Set.of(1L, 999L)))
                .isInstanceOf(EntityNotFoundException.class);
        assertBook(bookService.findById(1L).orElseThrow(),
                "BookTitle_1", "Author_1", "Genre_1", "Genre_2");
    }

    @DisplayName("Должен отклонять обновление отсутствующей книги без создания новой")
    @Test
    void shouldRejectMissingBookOnUpdateWithoutInsertingIt() {
        assertThatThrownBy(() -> bookService.update(999L, "invalid", 1L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book with id 999 not found");
        assertThat(bookService.findAll()).hasSize(3);
        assertThat(bookService.findById(999L)).isEmpty();
    }

    @DisplayName("Должен сохранять комментарии и заменять жанры при обновлении книги")
    @Test
    void shouldKeepCommentsAndReplaceGenreLinksOnUpdate() {
        var updated = bookService.update(1L, "editedBook", 2L, Set.of(3L));
        assertBook(updated, "editedBook", "Author_2", "Genre_3");
        assertBook(bookService.findById(1L).orElseThrow(), "editedBook", "Author_2", "Genre_3");
        assertThat(jdbc.queryForList("select genre_id from books_genres where book_id = 1", Long.class))
                .containsExactly(3L);
        assertThat(jdbc.queryForObject("select count(*) from comments where book_id = 1", Long.class)).isEqualTo(1);
        assertThat(bookService.findAll()).hasSize(3);
    }

    private record BookView(String title, String author, List<String> genres) {
    }

    private void assertBook(Book book, String title, String author, String... genres) {
        var actual = new BookView(book.getTitle(), book.getAuthor().getFullName(),
                book.getGenres().stream().map(genre -> genre.getName()).sorted().toList());
        var expected = new BookView(title, author, Arrays.stream(genres).sorted().toList());
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        assertThat(bookConverter.bookToString(book)).contains(title, author);
    }
}
