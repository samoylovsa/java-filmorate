package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public List<GenreResponse> getAllGenres() {
        return genreStorage.findAllGenres().stream()
                .map(GenreMapper::mapToGenreResponse)
                .toList();
    }

    public GenreResponse getGenreById(int id) {
        Genre genre = genreStorage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID: " + id + " не найден"));
        return GenreMapper.mapToGenreResponse(genre);
    }

    public Map<Long, List<Genre>> getFilmGenresMap() {
        return genreStorage.getFilmGenres();
    }
}