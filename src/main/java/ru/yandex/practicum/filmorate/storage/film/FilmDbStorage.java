package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("rating_id", film.getMpaId());

        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        log.info("В таблицу films добавлен фильм с ID: {}", id);

        return Film.builder()
                .filmId(id)
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpaId(film.getMpaId())
                .build();
    }

    @Override
    public void addFilmGenres(Long filmId, Set<Integer> genreIds) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", filmId);

        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?) "
                + "ON CONFLICT (film_id, genre_id) DO NOTHING";

        List<Object[]> batchArgs = genreIds.stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);

        log.debug("Добавлены жанры: {} для фильма с ID: {}", genreIds, filmId);
    }

    @Override
    public Set<Integer> getFilmGenres(Long filmId) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = ?";

        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt("genre_id"),
                filmId
        ));
    }

    @Override
    public Film updateFilm(Film film) {
        return null;
    }

    @Override
    public List<Film> findAllFilms() {
        return List.of();
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return Optional.empty();
    }

    @Override
    public List<Film> findTopPopularFilms(int count) {
        return List.of();
    }
}
