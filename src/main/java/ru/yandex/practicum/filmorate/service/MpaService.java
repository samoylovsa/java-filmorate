package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.MpaResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public List<MpaResponse> getAllMpaRatings() {
        return mpaDbStorage.findAllMpaRatings().stream()
                .map(MpaMapper::mapToMpaResponse)
                .collect(Collectors.toList());
    }

    public MpaResponse getMpaRatingById(int id) {
        return MpaMapper.mapToMpaResponse(
                mpaDbStorage.findMpaRatingById(id)
                        .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID: " + id + " не найден"))
        );
    }
}