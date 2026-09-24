package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.services.BookCommentService;

import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RequiredArgsConstructor
@ShellComponent
public class BookCommentCommands {

    private final BookCommentService bookCommentService;

    private final BookCommentConverter bookCommentConverter;

    @ShellMethod(value = "Find by id comment", key = "cbid")
    public String findCommentById(long id) {
        return bookCommentService.findById(id)
                .map(bookCommentConverter::commentToString)
                .orElse("Comment with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Find comments by book id", key = "cbbid")
    public String findCommentsByBookId(long id) {
        return bookCommentService.findByBookId(id).stream()
                .map(bookCommentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert comment (e.g.: cins \"Great book\" 1)", key = "cins")
    public String insertComment(String text, long bookId) {
        var savedComment = bookCommentService.insert(text, bookId);
        return bookCommentConverter.commentToString(savedComment);
    }

    @ShellMethod(value = "Update comment (e.g.: cupd 4 \"Updated review\" 1)", key = "cupd")
    public String updateComment(long id, String text, long bookId) {
        var savedComment = bookCommentService.update(id, text, bookId);
        return bookCommentConverter.commentToString(savedComment);
    }

    @ShellMethod(value = "Delete comment by id (e.g.: cdel 4)", key = "cdel")
    public void deleteComment(long id) {
        bookCommentService.deleteById(id);
    }
}
