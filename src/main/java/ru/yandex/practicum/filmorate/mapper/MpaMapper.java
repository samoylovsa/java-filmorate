package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.model.MpaRating;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaMapper {

    public static MpaResponse mapToMpaResponse(MpaRating mpaRating) {
        return MpaResponse.builder()
                .id(mpaRating.getId())
                .name(mpaRating.getName())
                .build();
    }
}