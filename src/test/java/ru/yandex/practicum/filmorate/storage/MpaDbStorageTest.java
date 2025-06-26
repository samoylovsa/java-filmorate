package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void testFindAllMpaRatingsShouldReturnAllRatingsSortedById() {
        List<MpaRating> ratings = mpaStorage.findAllMpaRatings();

        assertThat(ratings)
                .hasSize(5)
                .extracting(MpaRating::getId)
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(ratings)
                .extracting(MpaRating::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindMpaRatingByIdShouldReturnRatingWhenExists() {
        Optional<MpaRating> ratingOptional = mpaStorage.findMpaRatingById(2);

        assertThat(ratingOptional)
                .isPresent()
                .hasValueSatisfying(rating -> {
                    assertThat(rating.getId()).isEqualTo(2);
                    assertThat(rating.getName()).isEqualTo("PG");
                });
    }

    @Test
    void testFindMpaRatingByIdShouldReturnEmptyOptionalWhenNotExists() {
        Optional<MpaRating> ratingOptional = mpaStorage.findMpaRatingById(999);

        assertThat(ratingOptional).isEmpty();
    }

    @Test
    void testFindMpaRatingByIdShouldHandleInvalidId() {
        Optional<MpaRating> ratingOptional = mpaStorage.findMpaRatingById(-1);

        assertThat(ratingOptional).isEmpty();
    }
}