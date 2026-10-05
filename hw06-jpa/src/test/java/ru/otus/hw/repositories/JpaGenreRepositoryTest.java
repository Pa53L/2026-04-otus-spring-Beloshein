package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaGenreRepository.class)
@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
class JpaGenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @DisplayName("Должен загружать все жанры в порядке id")
    @Test
    void shouldReturnAllGenresInIdOrder() {
        assertThat(repository.findAll()).extracting(Genre::getName)
                .containsExactly("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
    }

    @DisplayName("Должен выбирать только существующие жанры из указанных id")
    @Test
    void shouldSelectOnlyExistingRequestedGenres() {
        assertThat(repository.findAllByIds(Set.of(5L, 1L, 999L))).extracting(Genre::getId)
                .containsExactly(1L, 5L);
    }
}
