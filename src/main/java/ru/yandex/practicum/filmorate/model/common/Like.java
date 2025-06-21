package ru.yandex.practicum.filmorate.model.common;

import lombok.Data;

@Data
public class Like {
    private Long filmId;
    private Long userId;
}