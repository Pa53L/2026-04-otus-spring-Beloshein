package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Genre;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({GenreServiceImpl.class})
@Transactional(propagation = Propagation.NEVER)
@DisplayName("Сервис для работы с жанрами")
class GenreServiceImplTest {

    @Autowired
    private GenreService service;

    @DisplayName("Должен загружать список всех жанров")
    @Test
    void shouldReadAllGenres() {
        assertThat(service.findAll()).extracting(Genre::getName)
                .containsExactly("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
    }
}
