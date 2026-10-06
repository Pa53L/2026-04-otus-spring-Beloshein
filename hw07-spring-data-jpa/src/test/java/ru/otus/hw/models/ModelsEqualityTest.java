package ru.otus.hw.models;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Равенство и хеш сущностей")
class ModelsEqualityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Разные новые авторы не должны быть равны")
    void shouldDistinguishNewAuthors() {
        var firstAuthor = new Author(null, "Автор");
        var secondAuthor = new Author(null, "Автор");

        assertThat(firstAuthor.equals(firstAuthor)).isTrue();
        assertThat(firstAuthor.equals(secondAuthor)).isFalse();
        assertThat(secondAuthor.equals(firstAuthor)).isFalse();

        var authors = new HashSet<Author>();
        authors.add(firstAuthor);
        authors.add(secondAuthor);

        assertThat(authors).hasSize(2);
    }

    @Test
    @DisplayName("Автор и его proxy должны быть равны без загрузки из БД")
    void shouldEqualDetachedProxy() {
        var author = em.persistAndFlush(new Author(null, "Автор"));
        em.clear();

        var proxy = em.getEntityManager()
                .getReference(Author.class, author.getId());
        em.clear();

        assertThat(Hibernate.isInitialized(proxy)).isFalse();

        assertThat(author.equals(proxy)).isTrue();
        assertThat(proxy.equals(author)).isTrue();
        assertThat(proxy.hashCode()).isEqualTo(author.hashCode());

        assertThat(Hibernate.isInitialized(proxy)).isFalse();
    }

    @Test
    @DisplayName("Хеш не должен меняться после сохранения и изменения автора")
    void shouldKeepHashCodeStable() {
        var author = new Author(null, "Автор");
        var initialHash = author.hashCode();
        var authors = new HashSet<Author>();
        authors.add(author);

        em.persistAndFlush(author);

        assertThat(author.getId()).isNotNull();
        assertThat(author.hashCode()).isEqualTo(initialHash);
        assertThat(authors.contains(author)).isTrue();

        author.setFullName("Новое имя");
        em.flush();

        assertThat(author.hashCode()).isEqualTo(initialHash);
        assertThat(authors.contains(author)).isTrue();
    }
}
