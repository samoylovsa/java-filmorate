package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {

    FilmStorage filmStorage;
    UserStorage userStorage;

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
        Film film = filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (film.getLikedUserIds().contains(userId)) {
            throw new ValidationException("Пользователь " + userId + " уже лайкнул этот фильм");
        }

        film.getLikedUserIds().add(userId);

        filmStorage.updateFilm(film);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

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

        List<Film> allFilms = filmStorage.findAllFilms();

        List<Film> sortedFilms = allFilms.stream()
                .sorted((f1, f2) -> {
                    int compare = Integer.compare(
                            f2.getLikedUserIds().size(),
                            f1.getLikedUserIds().size()
                    );
                    return compare != 0 ? compare : Long.compare(f1.getId(), f2.getId());
                })
                .toList();

        return sortedFilms.stream()
                .limit(count)
                .toList();
    }
}
