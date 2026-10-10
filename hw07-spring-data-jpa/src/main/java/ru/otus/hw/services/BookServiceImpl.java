package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public Book insert(String title, long authorId, Set<Long> genresIds) {
        var author = requireAuthor(authorId);
        var genres = requireGenres(genresIds);
        return bookRepository.save(new Book(title, author, genres));
    }

    @Override
    @Transactional
    public Book update(long id, String title, long authorId, Set<Long> genresIds) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
        var author = requireAuthor(authorId);
        var genres = requireGenres(genresIds);

        book.setTitle(title);
        book.setAuthor(author);
        book.getGenres().clear();
        book.getGenres().addAll(genres);
        return book;
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    private Author requireAuthor(long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
    }

    private List<Genre> requireGenres(Set<Long> genreIds) {
        if (isEmpty(genreIds)) {
            throw new IllegalArgumentException("At least one genre is required");
        }
        var genres = genreRepository.findAllById(genreIds);
        var missingIds = new TreeSet<>(genreIds);
        genres.forEach(genre -> missingIds.remove(genre.getId()));
        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException("Genres with ids %s not found".formatted(missingIds));
        }
        return genres;
    }
}
