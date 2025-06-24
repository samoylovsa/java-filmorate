package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.request.FilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(FilmRequest filmRequest) {
        return Film.builder()
                .filmId(filmRequest.getId()) // Просто копируем ID, если есть
                .name(filmRequest.getName())
                .description(filmRequest.getDescription())
                .releaseDate(filmRequest.getReleaseDate())
                .duration(filmRequest.getDuration())
                .mpaId(filmRequest.getMpa() != null ? filmRequest.getMpa().getId() : null)
                .build();
    }

    public static FilmResponse mapToFilmResponse(Film film, Set<Integer> genres) {
        return FilmResponse.builder()
                .id(film.getFilmId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpaId() != null ? new MpaRating(film.getMpaId()) : null)
                .genres(genres != null && !genres.isEmpty() ? genres : null)
                .build();
    }
}