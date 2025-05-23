package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {

    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);

        findUserById(userId);

        if (film.getLikedUserIds().contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " уже лайкнул этот фильм");
        }

        film.getLikedUserIds().add(userId);

        filmStorage.updateFilm(film);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);

        findUserById(userId);

        if (!film.getLikedUserIds().contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " не лайкал этот фильм или уже удалил лайк");
        }

        film.getLikedUserIds().remove(userId);

        filmStorage.updateFilm(film);
    }

    public List<Film> getTopPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        return filmStorage.findTopPopularFilms(count);
    }

    private Film findFilmById(Long filmId) {
        return filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    private User findUserById(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}
