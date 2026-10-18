package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BookForm {

    @NotBlank(message = "Введите название книги")
    private String title;

    @NotNull(message = "Выберите автора")
    @Positive(message = "Некорректный идентификатор автора")
    private Long authorId;

    @NotEmpty(message = "Выберите хотя бы один жанр")
    private List<@NotNull @Positive Long> genreIds = new ArrayList<>();
}