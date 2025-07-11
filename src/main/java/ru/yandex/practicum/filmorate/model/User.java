package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private Long userId;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}