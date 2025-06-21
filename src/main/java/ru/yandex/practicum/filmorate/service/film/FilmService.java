package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private FilmStorage filmStorage;
    private UserStorage userStorage;

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);

        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);

        return filmStorage.updateFilm(film);
    }

    public List<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);
        validateUserExists(userId);

        validateNotLiked(film, userId);

        film.getLikedUserIds().add(userId);

        filmStorage.updateFilm(film);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);
        validateUserExists(userId);

        validateLiked(film, userId);

        film.getLikedUserIds().remove(userId);

        filmStorage.updateFilm(film);
    }

    public List<Film> getTopPopularFilms(int count) {
        validateCountParameter(count);

        return filmStorage.findTopPopularFilms(count);
    }

    private Film findFilmById(Long filmId) {
        return filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    private User validateUserExists(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateNotLiked(Film film, Long userId) {
        if (film.getLikedUserIds().contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " уже лайкнул этот фильм");
        }
    }

    private void validateLiked(Film film, Long userId) {
        if (!film.getLikedUserIds().contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " не лайкал этот фильм или уже удалил лайк");
        }
    }

    private void validateCountParameter(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
    }

    private void validateFilm(Film film) {
        log.debug("Начало валидации фильма: {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации фильма: название не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Превышена длина описания фильма ({} символов). Максимум 200",
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Некорректная дата релиза фильма: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Валидация фильма пройдена успешно");
    }
}
