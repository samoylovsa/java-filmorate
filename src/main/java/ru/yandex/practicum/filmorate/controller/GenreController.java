package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.response.GenreResponse;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    @Autowired
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<GenreResponse> getAllGenres() {
        log.info("Получен запрос на получение списка всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public GenreResponse getGenreById(@PathVariable int id) {
        log.info("Получен запрос на получение жанра по ID: {}", id);
        return genreService.getGenreById(id);
    }
}