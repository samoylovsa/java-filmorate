package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private int idCounter = 1;
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public Film addFilm(Film film) {
        try {
            validateFilm(film);
            film.setId(idCounter++);
            films.put(film.getId(), film);
            log.info("Добавлен новый фильм с ID: {}", film.getId());
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        try {
            if (film.getId() == null || !films.containsKey(film.getId())) {
                throw new ValidationException("Не найден фильм с ID: " + film.getId());
            }
            validateFilm(film);
            films.put(film.getId(), film);
            log.info("Фильм с ID {} успешно обновлен", film.getId());
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    private void validateFilm(Film film) {
        log.debug("Начало валидации фильма: {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации фильма: название не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Превышена длина описания фильма ({} символов). Максимум 200",
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Некорректная дата релиза фильма: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Валидация фильма пройдена успешно");
    }
}
