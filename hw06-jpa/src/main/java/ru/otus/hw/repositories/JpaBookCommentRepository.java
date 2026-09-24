package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaBookCommentRepository implements BookCommentRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<BookComment> findById(long id) {
        return Optional.ofNullable(em.find(BookComment.class, id, Map.of(
                "jakarta.persistence.fetchgraph", em.getEntityGraph("BookComment.summary"))));
    }

    @Override
    public List<BookComment> findByBookId(long bookId) {
        return em.createQuery("""
                select c
                from BookComment c
                where c.book.id = :bookId
                order by c.id
                """, BookComment.class)
                .setHint("jakarta.persistence.fetchgraph", em.getEntityGraph("BookComment.summary"))
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public BookComment save(BookComment comment) {
        if (comment.getId() == null) {
            em.persist(comment);
            return comment;
        }
        return em.contains(comment) ? comment : em.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        var comment = em.find(BookComment.class, id);
        if (comment != null) {
            em.remove(comment);
        }
    }
}
