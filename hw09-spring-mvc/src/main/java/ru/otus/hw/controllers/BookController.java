package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.HashSet;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookCommentService commentService;

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String list(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String details(@PathVariable("id") long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("book", book);
        model.addAttribute("comments", commentService.findByBookId(id));
        return "books/details";
    }

    @GetMapping("/books/new")
    public String createForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "books/create";
    }

    @PostMapping("/books")
    public String create(
            @Valid @ModelAttribute("bookForm") BookForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());

            return "books/create";
        }

        bookService.insert(
                form.getTitle(),
                form.getAuthorId(),
                new HashSet<>(form.getGenreIds()));

        return "redirect:/books";
    }

    @GetMapping("/books/{id}/edit")
    public String editForm(@PathVariable("id") long id, Model model) {
        var book = bookService.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        var form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthorId(book.getAuthor().getId());
        form.setGenreIds(book.getGenres().stream()
                .map(Genre::getId)
                .toList());

        model.addAttribute("bookForm", form);
        model.addAttribute("bookId", id);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());

        return "books/edit";
    }

    @PostMapping("/books/{id}")
    public String update(
            @PathVariable("id") long id,
            @Valid @ModelAttribute("bookForm") BookForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());

            return "books/edit";
        }

        bookService.update(id,
                form.getTitle(),
                form.getAuthorId(),
                new HashSet<>(form.getGenreIds()));

        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable("id") long id) {

        bookService.deleteById(id);

        return "redirect:/books";
    }
}
