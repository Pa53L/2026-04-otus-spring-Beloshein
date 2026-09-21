package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Genre> findAll() {
        return jdbc.query("""
                select id, name
                from genres
                order by id
                """, new GenreRowMapper());
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {

        var params = new MapSqlParameterSource("ids", ids);

        var sql = """
                select id, name
                from genres
                where id in (:ids)
                order by id
                """;

        return jdbc.query(sql, params, new GenreRowMapper());
    }

    private static class GenreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int i) throws SQLException {
            return new Genre(
                    rs.getLong("id"),
                    rs.getString("name")
            );
        }
    }
}
