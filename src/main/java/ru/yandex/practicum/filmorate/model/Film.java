package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Film {
    private Long filmId;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Integer mpaId;
}