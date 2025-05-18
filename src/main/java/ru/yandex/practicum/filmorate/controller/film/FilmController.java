package ru.yandex.practicum.filmorate.controller.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    FilmStorage filmStorage;

    @Autowired
    FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());
        return filmStorage.updateFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на список всех фильмов.");
        return filmStorage.findAllFilms();
    }
}