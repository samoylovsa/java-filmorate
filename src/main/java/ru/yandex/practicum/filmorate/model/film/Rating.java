package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;

@Data
public class Rating {
    private Integer ratingId;
    private String name;
    private String description;
}
