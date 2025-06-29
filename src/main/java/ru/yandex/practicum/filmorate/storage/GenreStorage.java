package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> findAllGenres();

    Optional<Genre> findGenreById(int id);

    Map<Long, List<Genre>> getFilmGenres();
}
