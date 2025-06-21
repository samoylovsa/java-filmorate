package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.*;

@Slf4j
@Component
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private Long idCounter = 1L;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        film.setFilmId(idCounter++);
        films.put(film.getFilmId(), film);
        log.info("Добавлен новый фильм с ID: {}", film.getFilmId());

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getFilmId() == null || !films.containsKey(film.getFilmId())) {
            throw new NotFoundException("Не найден фильм с ID: " + film.getFilmId());
        }
        films.put(film.getFilmId(), film);
        log.info("Фильм с ID {} успешно обновлен", film.getFilmId());

        return film;
    }

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public List<Film> findTopPopularFilms(int count) {
        return films.values().stream()
                .sorted(Comparator
                        .comparingInt((Film f) -> f.getLikedUserIds().size()).reversed()
                        .thenComparing(Film::getFilmId)
                )
                .limit(count)
                .toList();
    }
}
