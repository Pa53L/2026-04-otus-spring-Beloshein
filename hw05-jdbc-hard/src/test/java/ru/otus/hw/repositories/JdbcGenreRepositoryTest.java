package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с жанрами ")
@JdbcTest
@Import({JdbcGenreRepository.class})
public class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repository;

    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        this.dbGenres = getDbGenres();
    }

    @Test
    @DisplayName("Должен загрузить список всех жанров")
    void shouldReturnCorrectGenreList() {
        var actualGenres = repository.findAll();

        assertThat(actualGenres).containsExactlyElementsOf(dbGenres);
        actualGenres.forEach(System.out::println);
    }

    @Test
    void shouldReturnCorrectGenreListByIds() {
        var idSet = Set.of(1L, 2L, 3L);

        var actual = repository.findAllByIds(idSet);
        assertThat(actual.size()).isEqualTo(3);

        var actualIdSet = actual.stream().map(Genre::getId).collect(Collectors.toSet());
        assertThat(actualIdSet).containsExactlyInAnyOrderElementsOf(idSet);
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }

}
