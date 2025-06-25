package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<MpaRating> mpaMapper;

    @Override
    public List<MpaRating> findAllMpaRatings() {
        String sql = "SELECT rating_id, name FROM ratings";

        List<MpaRating> ratings = jdbcTemplate.query(sql, mpaMapper);

        return ratings.stream()
                .sorted(Comparator.comparingInt(MpaRating::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MpaRating> findMpaRatingById(int id) {
        String sql = "SELECT rating_id, name FROM ratings WHERE rating_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mpaMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
