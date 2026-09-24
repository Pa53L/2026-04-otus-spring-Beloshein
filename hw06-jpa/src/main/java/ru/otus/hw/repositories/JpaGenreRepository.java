package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

@Repository
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Genre> findAll() {
        return em.createQuery(
                "SELECT g FROM Genre g ORDER BY g.id", Genre.class
        ).getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {

        return em.createQuery("select g from Genre g where g.id in :ids order by g.id", Genre.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
