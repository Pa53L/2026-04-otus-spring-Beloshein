package ru.otus.hw.repositories;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.BookServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaBookRepository.class, JpaBookCommentRepository.class, BookConverter.class,
        AuthorConverter.class, GenreConverter.class, BookCommentConverter.class,
        BookServiceImpl.class, JpaAuthorRepository.class, JpaGenreRepository.class})
@DisplayName("Загрузка связей без проблемы N+1")
class JpaFetchPlanTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookService books;

    @Autowired
    private BookCommentRepository comments;

    @Autowired
    private BookConverter bookConverter;

    @Autowired
    private BookCommentConverter commentConverter;

    @AfterEach
    void stopCountingQueries() {
        statistics().clear();
        statistics().setStatisticsEnabled(false);
    }

    @DisplayName("Должен загружать каталог двумя запросами независимо от числа книг")
    @ParameterizedTest
    @ValueSource(ints = {1, 50})
    void shouldReadCatalogWithTwoQueriesRegardlessOfSize(int extraBooks) {
        for (int i = 0; i < extraBooks; i++) {
            createBook(i);
        }
        startCountingQueries();

        var catalog = books.findAll();
        assertThat(catalog).hasSize(3 + extraBooks);
        assertThat(catalog).extracting(Book::getId).doesNotHaveDuplicates();
        catalog.forEach(this::assertCatalogLoaded);
        assertThat(statistics().getPrepareStatementCount()).isEqualTo(2);

        em.clear();
        catalog.forEach(book -> assertThat(bookConverter.bookToString(book))
                .contains(book.getTitle(), book.getAuthor().getFullName(), book.getGenres().get(0).getName()));
        assertThat(statistics().getPrepareStatementCount()).isEqualTo(2);
    }

    @DisplayName("Должен загружать комментарии одним запросом независимо от их числа")
    @ParameterizedTest
    @ValueSource(ints = {1, 50})
    void shouldReadCommentSummariesWithOneQueryRegardlessOfSize(int count) {
        var book = createBook(0);
        for (int i = 0; i < count; i++) {
            em.persist(new BookComment("Review_" + i, book));
        }
        var bookId = book.getId();
        startCountingQueries();

        var summaries = comments.findByBookId(bookId);
        assertThat(summaries).hasSize(count);
        summaries.forEach(this::assertOnlyBookLoaded);
        em.clear();
        summaries.forEach(comment -> assertThat(commentConverter.commentToString(comment))
                .contains(comment.getText(), "Catalog_0"));
        assertThat(statistics().getPrepareStatementCount()).isEqualTo(1);
    }

    private Book createBook(int index) {
        var author = em.persist(new Author(null, "Writer_" + index));
        var genre = em.persist(new Genre(null, "Category_" + index));
        var secondGenre = em.persist(new Genre(null, "Additional_" + index));
        return em.persist(new Book("Catalog_" + index, author, List.of(genre, secondGenre)));
    }

    private void startCountingQueries() {
        em.flush();
        em.clear();
        em.getEntityManager().getEntityManagerFactory().getCache().evictAll();
        statistics().setStatisticsEnabled(true);
        statistics().clear();
    }

    private Statistics statistics() {
        return em.getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    private void assertCatalogLoaded(Book book) {
        assertThat(Hibernate.isInitialized(book.getAuthor())).isTrue();
        assertThat(Hibernate.isInitialized(book.getGenres())).isTrue();
        assertThat(book.getAuthor().getFullName()).isNotBlank();
        assertThat(book.getGenres()).extracting(Genre::getName).doesNotContainNull();
    }

    private void assertOnlyBookLoaded(BookComment comment) {
        assertThat(Hibernate.isInitialized(comment.getBook())).isTrue();
        assertThat(comment.getBook().getTitle()).isEqualTo("Catalog_0");
        assertThat(Hibernate.isInitialized(comment.getBook().getAuthor())).isFalse();
        assertThat(Hibernate.isInitialized(comment.getBook().getGenres())).isFalse();
    }
}
