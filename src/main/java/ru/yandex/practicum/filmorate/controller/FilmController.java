package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.request.FilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public FilmResponse addFilm(@RequestBody FilmRequest filmRequest) {
        log.info("Получен запрос на добавление фильма: {}", filmRequest);
        return filmService.addFilm(filmRequest);
    }

    @PutMapping
    public FilmResponse updateFilm(@RequestBody FilmRequest filmRequest) {
        log.info("Получен запрос на обновление фильма с ID: {}", filmRequest.getId());
        return filmService.updateFilm(filmRequest);
    }

    @GetMapping
    public List<FilmResponse> getAllFilms() {
        log.info("Получен запрос на получение списка всех фильмов");
        return filmService.findAllFilms();
    }

    @GetMapping("/{id}")
    public FilmResponse getFilmById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма по ID: {}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        log.info("Получен запрос на установку лайка фильму c ID: {} от пользователя с ID: {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        log.info("Получен запрос на удаление лайка к фильму c ID: {} от пользователя с ID: {}", id, userId);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmResponse> getTopPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение топ {} популярных фильмов по лайкам", count);
        return filmService.getTopPopularFilms(count);
    }
}