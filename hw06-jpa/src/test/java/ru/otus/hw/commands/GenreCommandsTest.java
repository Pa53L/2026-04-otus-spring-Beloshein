package ru.otus.hw.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.models.Genre;

import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Команды для работы с жанрами")
class GenreCommandsTest {

    @DisplayName("Должен выводить список жанров")
    @Test
    void shouldPrintGenreList() {
        var service = mock(GenreService.class);
        when(service.findAll()).thenReturn(List.of(new Genre(3L, "Fantasy")));
        var commands = new GenreCommands(service, new GenreConverter());
        assertThat(commands.findAllGenres()).isEqualTo("Id: 3, Name: Fantasy");
    }
}
