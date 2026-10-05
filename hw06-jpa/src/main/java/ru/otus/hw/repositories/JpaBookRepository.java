package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Book> findById(long id) {
        return Optional.ofNullable(em.find(Book.class, id, catalogFetchPlan()));
    }

    @Override
    public List<Book> findAll() {
        var query = em.createQuery("select b from Book b order by b.id", Book.class);
        catalogFetchPlan().forEach(query::setHint);
        return query.getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.contains(book) ? book : em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        var book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }

    private Map<String, Object> catalogFetchPlan() {
        return Map.of("jakarta.persistence.fetchgraph", em.getEntityGraph("Book.catalog"));
    }
}
