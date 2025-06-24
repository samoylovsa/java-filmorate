package ru.yandex.practicum.filmorate.dto.response;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
