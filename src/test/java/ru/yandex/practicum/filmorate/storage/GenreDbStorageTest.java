package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, name) VALUES (3, 'Мультфильм')");
    }

    @Test
    void testFindAllGenresShouldReturnAllGenresSortedById() {
        List<Genre> genres = genreStorage.findAllGenres();

        assertThat(genres)
                .hasSize(3)
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм");
    }

    @Test
    void testFindAllGenresShouldReturnEmptyListWhenNoGenres() {
        jdbcTemplate.update("DELETE FROM genres");
        List<Genre> genres = genreStorage.findAllGenres();

        assertThat(genres).isEmpty();
    }

    @Test
    void testFindGenreByIdShouldReturnGenreWhenExists() {
        Optional<Genre> genreOptional = genreStorage.findGenreById(2);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(2);
                    assertThat(genre.getName()).isEqualTo("Драма");
                });
    }

    @Test
    void testFindGenreByIdShouldReturnEmptyOptionalWhenNotExists() {
        Optional<Genre> genreOptional = genreStorage.findGenreById(999);

        assertThat(genreOptional).isEmpty();
    }
}