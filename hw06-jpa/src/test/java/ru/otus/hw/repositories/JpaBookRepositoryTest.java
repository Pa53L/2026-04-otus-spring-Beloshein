package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaBookRepository.class)
@DisplayName("Репозиторий на основе Jpa для работы с книгами")
class JpaBookRepositoryTest {

    @Autowired
    private JpaBookRepository repository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("Должен сохранять новую книгу с автором и жанрами")
    @Test
    void shouldPersistNewBookWithRelations() {
        var book = new Book("New book", em.find(Author.class, 1L),
                List.of(em.find(Genre.class, 1L), em.find(Genre.class, 3L)));
        var id = repository.save(book).getId();
        em.flush();
        em.clear();
        var saved = repository.findById(id).orElseThrow();
        assertThat(id).isPositive();
        assertThat(saved.getTitle()).isEqualTo("New book");
        assertThat(saved.getAuthor().getFullName()).isEqualTo("Author_1");
        assertThat(saved.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1L, 3L);
    }

    @DisplayName("Должен обновлять отсоединённую книгу и её связи")
    @Test
    void shouldMergeDetachedBookWithChangedRelations() {
        var book = new Book(1L, "Updated", em.find(Author.class, 3L),
                List.of(em.find(Genre.class, 5L)));
        em.clear();
        repository.save(book);
        em.flush();
        em.clear();
        var saved = repository.findById(1L).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("Updated");
        assertThat(saved.getAuthor().getId()).isEqualTo(3L);
        assertThat(saved.getGenres()).extracting(Genre::getId).containsExactly(5L);
    }

}
