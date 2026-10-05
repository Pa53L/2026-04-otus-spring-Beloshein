package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.JpaAuthorRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AuthorServiceImpl.class, JpaAuthorRepository.class})
@Transactional(propagation = Propagation.NEVER)
@DisplayName("Сервис для работы с авторами")
class AuthorServiceImplTest {

    @Autowired
    private AuthorService service;

    @DisplayName("Должен загружать список всех авторов")
    @Test
    void shouldReadAllAuthors() {
        assertThat(service.findAll()).extracting(Author::getFullName)
                .containsExactly("Author_1", "Author_2", "Author_3");
    }

    @DisplayName("Должен находить автора по id или возвращать пустой результат")
    @Test
    void shouldFindAuthorOrReturnEmpty() {
        assertThat(service.findById(2L)).isPresent().get()
                .usingRecursiveComparison().isEqualTo(new Author(2L, "Author_2"));
        assertThat(service.findById(999L)).isEmpty();
    }
}
