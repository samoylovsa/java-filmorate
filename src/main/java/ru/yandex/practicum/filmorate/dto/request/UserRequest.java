package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequest {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
