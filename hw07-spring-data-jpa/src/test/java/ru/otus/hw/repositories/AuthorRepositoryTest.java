package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Spring Data Jpa для работы с авторами ")
@DataJpaTest
public class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @DisplayName("Должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var authors = authorRepository.findAllByOrderByIdAsc();
        assertThat(authors).extracting(Author::getFullName)
                .containsExactly("Author_1", "Author_2", "Author_3");
    }
}
