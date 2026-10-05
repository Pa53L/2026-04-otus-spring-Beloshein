package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaBookCommentRepository.class)
@DisplayName("Репозиторий на основе Jpa для работы с комментариями")
class JpaBookCommentRepositoryTest {

    @Autowired
    private JpaBookCommentRepository repository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("Должен сохранять новый комментарий со связью с книгой")
    @Test
    void shouldPersistCommentLinkedToBook() {
        var id = repository.save(new BookComment("New comment", em.find(Book.class, 1L))).getId();
        em.flush();
        em.clear();
        var saved = repository.findById(id).orElseThrow();
        assertThat(id).isPositive();
        assertThat(saved.getText()).isEqualTo("New comment");
        assertThat(saved.getBook().getTitle()).isEqualTo("BookTitle_1");
    }

    @DisplayName("Должен обновлять отсоединённый комментарий и менять его книгу")
    @Test
    void shouldMergeDetachedCommentAndMoveItToAnotherBook() {
        var comment = new BookComment(1L, "Updated", em.find(Book.class, 2L));
        em.clear();
        repository.save(comment);
        em.flush();
        em.clear();
        var saved = repository.findById(1L).orElseThrow();
        assertThat(saved.getText()).isEqualTo("Updated");
        assertThat(saved.getBook().getId()).isEqualTo(2L);
        assertThat(repository.findByBookId(1L)).isEmpty();
    }

    @DisplayName("Должен удалять только указанный комментарий")
    @Test
    void shouldDeleteOnlySelectedComment() {
        repository.deleteById(1L);
        em.flush();
        em.clear();
        assertThat(repository.findById(1L)).isEmpty();
        assertThat(repository.findById(2L)).isPresent();
    }
}
