package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.request.FilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaDbStorage;
    private final GenreService genreService;

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmResponse addFilm(FilmRequest filmRequest) {
        validateFilm(filmRequest);

        if (filmRequest.getMpa() != null) {
            mpaDbStorage.findMpaRatingById(filmRequest.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "MPA рейтинг с ID: " + filmRequest.getMpa().getId() + " не найден"
                    ));
        }

        if (filmRequest.getGenres() != null) {
            for (Genre genre : filmRequest.getGenres()) {
                genreService.getGenreById(genre.getId());
            }
        }


        Film film = FilmMapper.mapToFilm(filmRequest);
        film = filmStorage.addFilm(film);

        if (filmRequest.getGenres() != null && !filmRequest.getGenres().isEmpty()) {
            Set<Integer> genreIds = filmRequest.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            filmStorage.updateFilmGenres(film.getFilmId(), genreIds);
        }

        Set<Integer> responseGenres = filmStorage.getFilmGenres(film.getFilmId());

        return FilmMapper.mapToFilmResponse(
                film,
                responseGenres != null ? responseGenres : Collections.emptySet()
        );
    }

    public FilmResponse updateFilm(FilmRequest filmRequest) {
        validateFilm(filmRequest);

        findFilmById(filmRequest.getId());
        Film film = FilmMapper.mapToFilm(filmRequest);
        film = filmStorage.updateFilm(film);

        Set<Integer> genreIds = null;
        if (filmRequest.getGenres() != null) {
            genreIds = filmRequest.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            if (genreIds.isEmpty()) {
                filmStorage.deleteAllFilmGenres(film.getFilmId());
            } else {
                filmStorage.updateFilmGenres(film.getFilmId(), genreIds);
            }
        }

        Set<Integer> currentGenreIds = filmStorage.getFilmGenres(film.getFilmId());
        return FilmMapper.mapToFilmResponse(
                film,
                currentGenreIds != null ? currentGenreIds : Collections.emptySet()
        );
    }

    public List<FilmResponse> findAllFilms() {
        List<Film> films = filmStorage.findAllFilms();
        List<FilmResponse> listOfFilmResponse = new ArrayList<>(films.size());

        for (Film film : films) {
            Set<Integer> genres = filmStorage.getFilmGenres(film.getFilmId());
            FilmResponse filmResponse = FilmMapper.mapToFilmResponse(film, genres);
            listOfFilmResponse.add(filmResponse);
        }

        return listOfFilmResponse;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findFilmById(filmId);
        validateUserExists(userId);
        validateNotLiked(film.getFilmId(), userId);

        filmStorage.addLike(film.getFilmId(), userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        findFilmById(filmId);
        validateUserExists(userId);
        validateLiked(filmId, userId);

        filmStorage.deleteLike(filmId, userId);
    }

    public List<FilmResponse> getTopPopularFilms(int count) {
        validateCountParameter(count);

        List<Long> topFilmIds = filmStorage.findTopPopularFilmIds(count);
        if (topFilmIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Film> popularFilms = new ArrayList<>(topFilmIds.size());
        for (Long topFilmId : topFilmIds) {
            Film film = findFilmById(topFilmId);
            popularFilms.add(film);
        }

        List<FilmResponse> listOfFilmResponse = new ArrayList<>(popularFilms.size());
        for (Film popularFilm : popularFilms) {
            Set<Integer> genres = filmStorage.getFilmGenres(popularFilm.getFilmId());
            FilmResponse filmResponse = FilmMapper.mapToFilmResponse(popularFilm, genres);
            listOfFilmResponse.add(filmResponse);
        }

        return listOfFilmResponse;
    }

    private Film findFilmById(Long filmId) {
        return filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    private User validateUserExists(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateNotLiked(Long filmId, Long userId) {
        if (filmStorage.isFilmLikedByUser(filmId, userId)) {
            throw new ValidationException("Пользователь " + userId + " уже лайкнул этот фильм");
        }
    }

    private void validateLiked(Long filmId, Long userId) {
        if (!filmStorage.isFilmLikedByUser(filmId, userId)) {
            throw new ValidationException("Пользователь " + userId + " не лайкал этот фильм или уже удалил лайк");
        }
    }

    private void validateCountParameter(int count) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
    }

    private void validateFilm(FilmRequest filmRequest) {
        log.debug("Начало валидации фильма: {}", filmRequest);
        if (filmRequest.getName() == null || filmRequest.getName().isBlank()) {
            log.error("Ошибка валидации фильма: название не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (filmRequest.getDescription() != null && filmRequest.getDescription().length() > 200) {
            log.error("Превышена длина описания фильма ({} символов). Максимум 200",
                    filmRequest.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (filmRequest.getReleaseDate() == null || filmRequest.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Некорректная дата релиза фильма: {}", filmRequest.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (filmRequest.getDuration() == null || filmRequest.getDuration() <= 0) {
            log.error("Некорректная продолжительность фильма: {}", filmRequest.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Валидация фильма пройдена успешно");
    }
}
