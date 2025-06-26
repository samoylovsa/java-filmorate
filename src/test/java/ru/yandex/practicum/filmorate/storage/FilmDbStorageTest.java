package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    private Film testFilm1;
    private Film testFilm2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        testFilm1 = filmStorage.addFilm(createTestFilm("Film 1", 100, 1));
        testFilm2 = filmStorage.addFilm(createTestFilm("Film 2", 200, 2));
    }

    private Film createTestFilm(String name, int duration, int mpaId) {
        return Film.builder()
                .name(name)
                .description("Description for " + name)
                .releaseDate(LocalDate.now())
                .duration(duration)
                .mpaId(mpaId)
                .build();
    }

    @Test
    void testAddFilmShouldSaveFilmAndReturnWithGeneratedId() {
        Film newFilm = createTestFilm("New Film", 150, 3);
        Film savedFilm = filmStorage.addFilm(newFilm);

        assertThat(savedFilm)
                .isNotNull()
                .extracting(Film::getFilmId)
                .isNotNull();
        Optional<Film> retrievedFilm = filmStorage.findFilmById(savedFilm.getFilmId());
        assertThat(retrievedFilm).hasValueSatisfying(film ->
                assertThat(film)
                        .usingRecursiveComparison()
                        .ignoringFields("filmId")
                        .isEqualTo(newFilm)
        );
    }

    @Test
    void testUpdateFilmShouldUpdateFilmInDatabase() {
        Film updatedFilm = Film.builder()
                .filmId(testFilm1.getFilmId())
                .name("Updated Name")
                .description("Updated Desc")
                .releaseDate(LocalDate.of(2021, 2, 2))
                .duration(150)
                .mpaId(3)
                .build();
        Film result = filmStorage.updateFilm(updatedFilm);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(updatedFilm);
        Optional<Film> retrievedFilm = filmStorage.findFilmById(testFilm1.getFilmId());
        assertThat(retrievedFilm)
                .hasValueSatisfying(film ->
                        assertThat(film)
                                .usingRecursiveComparison()
                                .isEqualTo(updatedFilm)
                );
    }

    @Test
    void testFindAllFilmsShouldReturnAllFilms() {
        List<Film> films = filmStorage.findAllFilms();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Film 1", "Film 2");
    }

    @Test
    void testFindFilmByIdShouldReturnEmptyOptionalWhenNotFound() {
        Optional<Film> result = filmStorage.findFilmById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void testFindTopPopularFilmIdsShouldReturnOrderedByLikes() {
        filmStorage.addLike(testFilm1.getFilmId(), 1L);
        filmStorage.addLike(testFilm2.getFilmId(), 1L);
        filmStorage.addLike(testFilm2.getFilmId(), 2L);
        List<Long> popularIds = filmStorage.findTopPopularFilmIds(2);

        assertThat(popularIds)
                .containsExactly(testFilm2.getFilmId(), testFilm1.getFilmId());
    }

    @Test
    void testIsFilmLikedByUserShouldReturnTrueWhenLiked() {
        filmStorage.addLike(testFilm1.getFilmId(), 1L);
        boolean result = filmStorage.isFilmLikedByUser(testFilm1.getFilmId(), 1L);

        assertThat(result).isTrue();
    }

    @Test
    void testDeleteLikeShouldReturnTrueWhenLikeExists() {
        filmStorage.addLike(testFilm1.getFilmId(), 1L);
        boolean result = filmStorage.deleteLike(testFilm1.getFilmId(), 1L);

        assertThat(result).isTrue();
        assertThat(filmStorage.isFilmLikedByUser(testFilm1.getFilmId(), 1L)).isFalse();
    }
}