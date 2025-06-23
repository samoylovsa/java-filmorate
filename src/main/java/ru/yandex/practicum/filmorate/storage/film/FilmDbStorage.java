package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
    public void updateFilmGenres(Long filmId, Set<Integer> genreIds) {
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
    public void deleteAllFilmGenres(Long filmId) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", filmId);
        log.debug("Удалены все жанры для фильма с ID: {}", filmId);
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
        String sqlQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId(),
                film.getFilmId()
        );
        log.info("Фильм с ID {} успешно обновлен", film.getFilmId());

        return film;
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = "SELECT film_id, name, description, release_date, duration, rating_id FROM films";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        log.debug("Найдены фильмы: {}", films);
        return films;
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        String sql = "SELECT film_id, name, description, release_date, duration, rating_id " +
                "FROM films WHERE film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(
                    sql,
                    this::mapRowToFilm,
                    filmId
            );
            log.debug("Найден фильм с ID: {}", filmId);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Фильм с ID: {} не найден", filmId);
            return Optional.empty();
        }
    }

    @Override
    public List<Long> findTopPopularFilmIds(int count) {
        String sql = """
        SELECT film_id
        FROM likes
        GROUP BY film_id
        ORDER BY COUNT(user_id) DESC, film_id DESC
        LIMIT ?
        """;

        List<Long> topPopularFilmIds = jdbcTemplate.queryForList(sql, Long.class, count);

        log.debug("Найден следующий список популярных фильмов: {}", topPopularFilmIds);
        return topPopularFilmIds;
    }

    @Override
    public boolean isFilmLikedByUser(Long filmId, Long userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM likes WHERE film_id = ? AND user_id = ?)";
        boolean result = Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, filmId, userId)
        );

        log.debug("Фильм с ID: {} уже лайкнут пользователем с ID: {}", filmId, userId);
        return result;
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?) " +
                "ON CONFLICT (film_id, user_id) DO NOTHING";
        int affectedRows = jdbcTemplate.update(sql, filmId, userId);

        if (affectedRows > 0) {
            log.debug("Фильму с ID: {} добавлен лайк пользователем с ID: {}", filmId, userId);
        } else {
            log.debug("Фильму с ID: {} НЕ добавлен лайк пользователем с ID: {}", filmId, userId);
        }

        return affectedRows > 0;
    }

    @Override
    public boolean deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int affectedRows = jdbcTemplate.update(sql, filmId, userId);

        if (affectedRows > 0) {
            log.debug("Фильму с ID: {} удалён лайк пользователем с ID: {}", filmId, userId);
        } else {
            log.debug("Фильму с ID: {} НЕ удалён лайк пользователем с ID: {}", filmId, userId);
        }

        return affectedRows > 0;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .filmId(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getObject("description", String.class))
                .releaseDate(rs.getObject("release_date", LocalDate.class))
                .duration(rs.getObject("duration", Integer.class))
                .mpaId(rs.getObject("rating_id", Integer.class))
                .build();
    }
}
