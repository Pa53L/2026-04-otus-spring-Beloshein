package ru.otus.hw.repositories;

import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(value = "Book.catalog")
    Optional<Book> findById(@Nonnull Long id);

    @EntityGraph("Book.catalog")
    List<Book> findAllByOrderByIdAsc();
}
