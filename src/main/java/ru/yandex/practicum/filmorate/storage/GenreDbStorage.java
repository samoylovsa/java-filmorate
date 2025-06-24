package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> findAllGenres() {
        String sql = "SELECT genre_id, name FROM genres";

        log.info("Запрос всех жанров из базы данных");
        try {
            List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                return genre;
            });

            log.info("Успешно получено {} жанров из базы данных: {}", genres.size(), genres);
            return genres;
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка жанров из базы данных", e);
            throw new DataAccessException("Не удалось получить список жанров", e) {
            };
        }
    }

    public Optional<Genre> findGenreById(int id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";

        log.info("Поиск жанра по ID: {}", id);
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Genre g = new Genre();
                g.setId(rs.getInt("genre_id"));
                g.setName(rs.getString("name"));
                return g;
            }, id);

            log.info("Найден жанр с ID: {}, Название: {}", genre.getId(), genre.getName());
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Жанр с ID: {} не найден в базе данных", id);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Ошибка при поиске жанра по ID: {}", id, e);
            throw new DataAccessException("Не удалось найти жанр по ID: " + id, e) {
            };
        }
    }
}
