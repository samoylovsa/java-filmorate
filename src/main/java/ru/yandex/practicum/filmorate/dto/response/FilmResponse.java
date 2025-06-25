package ru.yandex.practicum.filmorate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class FilmResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaResponse mpa;
    private List<GenreResponse> genres;
}
