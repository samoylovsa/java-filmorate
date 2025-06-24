package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public List<MpaResponse> getAllMpaRatings() {
        log.info("Получен запрос на получение списка всех рейтингов");
        return mpaService.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaResponse getMpaRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение рейтинга по ID: {}", id);
        return mpaService.getMpaRatingById(id);
    }
}
