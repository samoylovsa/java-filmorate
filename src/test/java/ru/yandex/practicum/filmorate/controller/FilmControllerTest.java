package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.film.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private Film film;
    FilmStorage filmStorage;
    FilmService filmService;
    UserStorage userStorage;

    private Film createTestFilm(String name, String description, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(duration);
        return film;
    }

    private void assertThrowsValidationException(String expectedMessage, Runnable testCode) {
        ValidationException exception = assertThrows(ValidationException.class, testCode::run);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
        film = createTestFilm("Inception", "A movie about dreams", 148);
    }

    @Test
    void addFilmShouldReturnFilmWithIdWhenValidData() {
        Film addedFilm = filmController.addFilm(film);

        assertNotNull(addedFilm.getId());
        assertEquals("Inception", addedFilm.getName());
    }

    @Test
    void updateFilmShouldChangeDataWhenValidRequest() {
        Film addedFilm = filmController.addFilm(film);
        addedFilm.setDescription("New description");

        Film updatedFilm = filmController.updateFilm(addedFilm);

        assertEquals("New description", updatedFilm.getDescription());
    }

    @Test
    void getAllFilmsShouldReturnAllAddedFilms() {
        filmController.addFilm(film);
        Film secondFilm = createTestFilm("Interstellar", "Space adventure", 169);
        filmController.addFilm(secondFilm);

        assertEquals(2, filmController.getAllFilms().size());
    }

    @Test
    void addFilmShouldThrowExceptionWhenNameIsEmpty() {
        film.setName("");

        assertThrowsValidationException(
                "Название фильма не может быть пустым",
                () -> filmController.addFilm(film)
        );
    }

    @Test
    void addFilmShouldThrowExceptionWhenDescriptionTooLong() {
        film.setDescription("a".repeat(201));

        assertThrowsValidationException(
                "Максимальная длина описания — 200 символов",
                () -> filmController.addFilm(film)
        );
    }

    @Test
    void addFilmShouldThrowExceptionWhenInvalidReleaseDate() {
        film.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrowsValidationException(
                "Дата релиза не может быть раньше 28 декабря 1895 года",
                () -> filmController.addFilm(film)
        );
    }

    @Test
    void addFilmShouldThrowExceptionWhenNegativeDuration() {
        film.setDuration(-10);

        assertThrowsValidationException(
                "Продолжительность фильма должна быть положительным числом",
                () -> filmController.addFilm(film)
        );
    }

    @Test
    void updateFilmShouldThrowExceptionWhenFilmNotFound() {
        film.setId(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmController.updateFilm(film));

        assertEquals("Не найден фильм с ID: " + film.getId(), exception.getMessage());
    }
}