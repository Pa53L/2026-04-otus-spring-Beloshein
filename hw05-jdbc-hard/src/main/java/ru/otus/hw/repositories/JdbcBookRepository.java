package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<Book> findById(long id) {
        return Optional.ofNullable(jdbc.query("""
            select b.id as book_id, b.title as book_title,
                   a.id as author_id, a.full_name as author_full_name,
                   g.id as genre_id, g.name as genre_name
            from books b
            join authors a on a.id = b.author_id
            left join books_genres bg on bg.book_id = b.id
            left join genres g on g.id = bg.genre_id
            where b.id = :id
            order by g.id
            """,
                Map.of("id", id),
                new BookResultSetExtractor()));
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        jdbc.update("delete from books where id = :id", Map.of("id", id));
    }

    private List<Book> getAllBooksWithoutGenres() {
        return jdbc.query("""
            select b.id as book_id, b.title as book_title,
                   a.id as author_id, a.full_name as author_full_name
            from books b
            join authors a on a.id = b.author_id
            order by b.id
            """, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return jdbc.query("""
            select book_id, genre_id
            from books_genres
            order by book_id, genre_id
            """,
                (rs, rowNum) -> new BookGenreRelation(
                        rs.getLong("book_id"),
                        rs.getLong("genre_id")
                ));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        var booksById = booksWithoutGenres.stream().collect(Collectors.toMap(Book::getId, Function.identity()));
        var genresById = genres.stream().collect(Collectors.toMap(Genre::getId, Function.identity()));

        relations.forEach(relation -> {
            var book = booksById.get(relation.bookId());
            var genre = genresById.get(relation.genreId());
            if (book != null && genre != null) {
                book.getGenres().add(genre);
            }
        });
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId());

        jdbc.update("""
            insert into books(title, author_id)
            values (:title, :authorId)
            """, params, keyHolder, new String[]{"id"});
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var params = new MapSqlParameterSource()
                .addValue("id", book.getId())
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId());

        var updatedRows = jdbc.update("""
            update books
            set title = :title, author_id = :authorId
            where id = :id
            """, params);

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var params = book.getGenres().stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId())
                        .addValue("genreId", genre.getId()))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate("""
            insert into books_genres(book_id, genre_id)
            values (:bookId, :genreId)
            """, params);
    }

    private void removeGenresRelationsFor(Book book) {
        jdbc.update("""
            delete from books_genres
            where book_id = :bookId
            """, Map.of("bookId", book.getId()));
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Book(
                    rs.getLong("book_id"),
                    rs.getString("book_title"),
                    new Author(
                            rs.getLong("author_id"),
                            rs.getString("author_full_name")
                    ),
                    new ArrayList<>()
            );
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            while (rs.next()) {
                if (book == null) {
                    book = new Book(
                            rs.getLong("book_id"),
                            rs.getString("book_title"),
                            new Author(
                                    rs.getLong("author_id"),
                                    rs.getString("author_full_name")
                            ),
                            new ArrayList<>()
                    );
                }
                var genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    book.getGenres().add(new Genre(
                            genreId,
                            rs.getString("genre_name")
                    ));
                }
            }

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
