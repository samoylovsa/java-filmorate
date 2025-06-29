package ru.yandex.practicum.filmorate.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MpaResponse {
    int id;
    String name;
}