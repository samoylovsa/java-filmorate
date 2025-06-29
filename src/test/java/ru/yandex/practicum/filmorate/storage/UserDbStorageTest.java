package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testAddUserShouldSaveUserAndReturnWithGeneratedId() {
        User newUser = User.builder()
                .email("test@example.com")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User savedUser = userStorage.addUser(newUser);

        assertThat(savedUser.getUserId()).isPositive();
        assertThat(savedUser)
                .usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(newUser);
        Optional<User> retrievedUser = userStorage.findUserById(savedUser.getUserId());
        assertThat(retrievedUser).isPresent();
    }

    @Test
    void testUpdateUserShouldUpdateExistingUser() {
        User originalUser = userStorage.addUser(
                User.builder()
                        .email("original@example.com")
                        .login("originalLogin")
                        .name("Original Name")
                        .birthday(LocalDate.of(1990, 1, 1))
                        .build()
        );
        User updatedUser = User.builder()
                .userId(originalUser.getUserId())
                .email("updated@example.com")
                .login("updatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
        User result = userStorage.updateUser(updatedUser);

        assertThat(result).isEqualTo(updatedUser);
        Optional<User> retrievedUser = userStorage.findUserById(originalUser.getUserId());
        assertThat(retrievedUser).hasValue(updatedUser);
    }

    @Test
    void testFindAllUsersShouldReturnAllUsers() {
        User user1 = userStorage.addUser(
                User.builder()
                        .email("user1_" + System.currentTimeMillis() + "@example.com")
                        .login("login1_" + System.currentTimeMillis())
                        .name("User One")
                        .birthday(LocalDate.of(1990, 1, 1))
                        .build()
        );
        User user2 = userStorage.addUser(
                User.builder()
                        .email("user2_" + System.currentTimeMillis() + "@example.com")
                        .login("login2_" + System.currentTimeMillis())
                        .name("User Two")
                        .birthday(LocalDate.of(1995, 5, 5))
                        .build()
        );
        List<User> users = userStorage.findAllUsers();

        assertThat(users)
                .extracting(User::getUserId)
                .containsExactlyInAnyOrder(user1.getUserId(), user2.getUserId());
    }

    @Test
    void testFindUserByIdShouldReturnUserWhenExists() {
        User newUser = userStorage.addUser(
                User.builder()
                        .email("find@example.com")
                        .login("findLogin")
                        .name("Find Me")
                        .birthday(LocalDate.of(2000, 1, 1))
                        .build()
        );
        Optional<User> foundUser = userStorage.findUserById(newUser.getUserId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getEmail()).isEqualTo("find@example.com")
                );
    }

    @Test
    void testFindUserByIdShouldReturnEmptyWhenNotExists() {
        Optional<User> foundUser = userStorage.findUserById(999L);
        assertThat(foundUser).isEmpty();
    }
}