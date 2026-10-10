package ru.otus.hw.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import ru.otus.hw.services.BookCommentService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Команды для работы с комментариями")
class BookCommentCommandsTest {

    private final BookCommentService service = mock(BookCommentService.class);

    private final BookCommentCommands commands = new BookCommentCommands(service, new BookCommentConverter());

    private final BookComment comment = new BookComment(8L, "Review",
            new Book(7L, "Story", null, List.of()));

    private final String expected = "Id: 8, text: Review, book: {Id: 7, title: Story}";

    @DisplayName("Должен выводить комментарии и результаты поиска комментария")
    @Test
    void shouldPrintCommentsAndLookupResults() {
        when(service.findByBookId(7L)).thenReturn(List.of(comment));
        when(service.findById(8L)).thenReturn(Optional.of(comment));
        when(service.findById(999L)).thenReturn(Optional.empty());
        assertThat(commands.findCommentsByBookId(7L)).isEqualTo(expected);
        assertThat(commands.findCommentById(8L)).isEqualTo(expected);
        assertThat(commands.findCommentById(999L)).isEqualTo("Comment with id 999 not found");
    }

    @DisplayName("Должен передавать изменения комментария сервису и выводить результат")
    @Test
    void shouldForwardCommentEditsAndPrintServiceResults() {
        when(service.insert("Review", 7L)).thenReturn(comment);
        when(service.update(8L, "Review", 7L)).thenReturn(comment);
        assertThat(commands.insertComment("Review", 7L)).isEqualTo(expected);
        assertThat(commands.updateComment(8L, "Review", 7L)).isEqualTo(expected);
        commands.deleteComment(8L);
        verify(service).insert("Review", 7L);
        verify(service).update(8L, "Review", 7L);
        verify(service).deleteById(8L);
    }
}
