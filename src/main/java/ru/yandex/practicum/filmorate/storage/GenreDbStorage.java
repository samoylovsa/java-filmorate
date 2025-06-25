package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreMapper;

    @Override
    public List<Genre> findAllGenres() {
        String sql = "SELECT genre_id, name FROM genres";
        log.info("Запрос всех жанров из базы данных");
        try {
            List<Genre> genres = jdbcTemplate.query(sql, genreMapper);
            genres.sort(Comparator.comparingInt(Genre::getId));
            log.info("Успешно получено {} жанров из базы данных: {}", genres.size(), genres);
            return genres;
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка жанров из базы данных", e);
            throw new DataAccessException("Не удалось получить список жанров", e) {
            };
        }
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        log.info("Поиск жанра по ID: {}", id);
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreMapper, id);
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
