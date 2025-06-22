package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> findAllFilms();

    Optional<Film> findFilmById(Long filmId);

    List<Film> findTopPopularFilms(int count);

    void addFilmGenres(Long filmId, Set<Integer> genreIds);

    Set<Integer> getFilmGenres(Long filmId);
}
