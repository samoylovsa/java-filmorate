package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    @Autowired
    public GenreService(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    public List<GenreResponse> getAllGenres() {
        return genreDbStorage.findAllGenres().stream()
                .map(GenreMapper::mapToGenreResponse)
                .toList();
    }

    public GenreResponse getGenreById(int id) {
        Genre genre = genreDbStorage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID: " + id + " не найден"));
        return GenreMapper.mapToGenreResponse(genre);
    }
}