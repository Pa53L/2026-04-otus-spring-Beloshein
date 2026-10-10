package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({BookCommentServiceImpl.class, BookCommentConverter.class})
@Transactional(propagation = Propagation.NEVER)
@Sql(scripts = {"/reset-data.sql", "/test-data.sql"})
@DisplayName("Сервис для работы с комментариями")
class BookCommentServiceImplTest {

    @Autowired
    private BookCommentService commentService;

    @Autowired
    private BookCommentConverter converter;

    @DisplayName("Должен загружать комментарии книги с доступными вне транзакции связями")
    @Test
    void shouldFindCommentsByBookWithoutLazyException() {
        var comments = commentService.findByBookId(1L);
        assertThat(comments).hasSize(1);
        assertComment(comments.get(0), "Book 1 is interesting and exciting", 1L, "BookTitle_1");
    }

    @DisplayName("Должен сохранять комментарий с доступной вне транзакции книгой")
    @Test
    void shouldInsertNewCommentWithoutLazyException() {
        var comment = commentService.insert("newComment", 1L);
        assertThat(comment.getId()).isPositive();
        assertComment(comment, "newComment", 1L, "BookTitle_1");
        assertComment(commentService.findById(comment.getId()).orElseThrow(),
                "newComment", 1L, "BookTitle_1");
    }

    @DisplayName("Должен обновлять комментарий и переносить его к другой книге")
    @Test
    void shouldUpdateCommentAndMoveToAnotherBook() {
        var comment = commentService.update(1L, "editedComment", 2L);
        assertThat(comment.getId()).isEqualTo(1L);
        assertComment(comment, "editedComment", 2L, "BookTitle_2");
        assertComment(commentService.findById(1L).orElseThrow(), "editedComment", 2L, "BookTitle_2");
        assertThat(commentService.findByBookId(1L)).isEmpty();
    }

    @DisplayName("Должен сохранять комментарий без изменений при отсутствии указанной книги")
    @Test
    void shouldKeepCommentWhenTargetBookDoesNotExist() {
        assertThatThrownBy(() -> commentService.update(1L, "invalid", 999L))
                .isInstanceOf(EntityNotFoundException.class);
        assertComment(commentService.findById(1L).orElseThrow(),
                "Book 1 is interesting and exciting", 1L, "BookTitle_1");
    }

    private record CommentView(String text, long bookId, String bookTitle) {
    }

    private void assertComment(BookComment comment, String text, long bookId, String bookTitle) {
        var actual = new CommentView(comment.getText(), comment.getBook().getId(), comment.getBook().getTitle());
        assertThat(actual).usingRecursiveComparison().isEqualTo(new CommentView(text, bookId, bookTitle));
        assertThat(converter.commentToString(comment)).contains(text, bookTitle);
    }
}
