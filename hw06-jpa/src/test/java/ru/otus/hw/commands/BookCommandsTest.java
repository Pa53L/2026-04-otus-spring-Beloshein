package ru.otus.hw.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Команды для работы с книгами")
class BookCommandsTest {

    private final BookService service = mock(BookService.class);

    private final BookCommands commands = new BookCommands(service,
            new BookConverter(new AuthorConverter(), new GenreConverter()));

    private final Book book = new Book(7L, "Story", new Author(2L, "Writer"), List.of(new Genre(3L, "Fantasy")));

    private final String expected = "Id: 7, title: Story, author: {Id: 2, FullName: Writer}, "
            + "genres: [{Id: 3, Name: Fantasy}]";

    @DisplayName("Должен выводить каталог и результаты поиска книги")
    @Test
    void shouldPrintCatalogAndLookupResults() {
        when(service.findAll()).thenReturn(List.of(book));
        when(service.findById(7L)).thenReturn(Optional.of(book));
        when(service.findById(999L)).thenReturn(Optional.empty());
        assertThat(commands.findAllBooks()).isEqualTo(expected);
        assertThat(commands.findBookById(7L)).isEqualTo(expected);
        assertThat(commands.findBookById(999L)).isEqualTo("Book with id 999 not found");
    }

    @DisplayName("Должен передавать изменения книги сервису и выводить результат")
    @Test
    void shouldForwardBookEditsAndPrintServiceResults() {
        when(service.insert("Story", 2L, Set.of(3L))).thenReturn(book);
        when(service.update(7L, "Story", 2L, Set.of(3L))).thenReturn(book);
        assertThat(commands.insertBook("Story", 2L, Set.of(3L))).isEqualTo(expected);
        assertThat(commands.updateBook(7L, "Story", 2L, Set.of(3L))).isEqualTo(expected);
        commands.deleteBook(7L);
        verify(service).insert("Story", 2L, Set.of(3L));
        verify(service).update(7L, "Story", 2L, Set.of(3L));
        verify(service).deleteById(7L);
    }
}
