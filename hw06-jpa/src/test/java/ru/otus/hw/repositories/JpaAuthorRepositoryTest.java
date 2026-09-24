package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с авторами ")
@DataJpaTest
@Import(JpaAuthorRepository.class)
public class JpaAuthorRepositoryTest {

    @Autowired
    private JpaAuthorRepository jpaAuthorRepository;

    @DisplayName("Должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var authors = jpaAuthorRepository.findAll();
        assertThat(authors).extracting(Author::getFullName)
                .containsExactly("Author_1", "Author_2", "Author_3");
    }
}
