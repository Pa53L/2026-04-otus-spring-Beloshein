package ru.otus.hw.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.models.Author;

import ru.otus.hw.services.AuthorService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Команды для работы с авторами")
class AuthorCommandsTest {

    private final AuthorService service = mock(AuthorService.class);

    private final AuthorCommands commands = new AuthorCommands(service, new AuthorConverter());

    @DisplayName("Должен выводить список авторов")
    @Test
    void shouldPrintAuthorList() {
        when(service.findAll()).thenReturn(List.of(new Author(2L, "Test author")));
        assertThat(commands.findAllAuthors()).isEqualTo("Id: 2, FullName: Test author");
    }

    @DisplayName("Должен выводить автора или сообщение об его отсутствии")
    @Test
    void shouldPrintFoundAuthorOrMissingMessage() {
        when(service.findById(2L)).thenReturn(Optional.of(new Author(2L, "Test author")));
        when(service.findById(999L)).thenReturn(Optional.empty());
        assertThat(commands.findAuthorById(2L)).isEqualTo("Id: 2, FullName: Test author");
        assertThat(commands.findAuthorById(999L)).isEqualTo("Author with id 999 not found");
    }
}
