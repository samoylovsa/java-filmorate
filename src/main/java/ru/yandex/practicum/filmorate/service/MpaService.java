package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public List<MpaResponse> getAllMpaRatings() {
        return mpaStorage.findAllMpaRatings().stream()
                .map(MpaMapper::mapToMpaResponse)
                .collect(Collectors.toList());
    }

    public MpaResponse getMpaRatingById(int id) {
        return MpaMapper.mapToMpaResponse(
                mpaStorage.findMpaRatingById(id)
                        .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID: " + id + " не найден"))
        );
    }
}