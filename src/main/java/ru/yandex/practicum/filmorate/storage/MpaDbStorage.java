package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<MpaRating> findAllMpaRatings() {
        String sql = "SELECT rating_id, name FROM ratings";

        List<MpaRating> ratings = jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaRating(
                        rs.getInt("rating_id"),
                        rs.getString("name")
                ));

        return ratings.stream()
                .sorted(Comparator.comparingInt(MpaRating::getId))
                .collect(Collectors.toList());
    }

    public Optional<MpaRating> findMpaRatingById(int id) {
        String sql = "SELECT rating_id, name FROM ratings WHERE rating_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new MpaRating(
                            rs.getInt("rating_id"),
                            rs.getString("name")
                    ), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
