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

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendShipDbStorage.class, UserRowMapper.class, UserDbStorage.class})
class FriendShipDbStorageTest {

    private final FriendShipDbStorage friendShipStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
    }

    private User createTestUser(String email, String login) {
        User user = User.builder()
                .email(email)
                .login(login)
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        return userStorage.addUser(user);
    }

    @Test
    void testAddFriendShouldCreateFriendship() {
        User user1 = createTestUser("user1@test.com", "login1");
        User user2 = createTestUser("user2@test.com", "login2");
        friendShipStorage.addFriend(user1.getUserId(), user2.getUserId());

        assertThat(friendShipStorage.friendshipExists(user1.getUserId(), user2.getUserId())).isTrue();
    }

    @Test
    void testDeleteFriendShouldRemoveFriendship() {
        User user1 = createTestUser("user1@test.com", "login1");
        User user2 = createTestUser("user2@test.com", "login2");
        friendShipStorage.addFriend(user1.getUserId(), user2.getUserId());
        friendShipStorage.deleteFriend(user1.getUserId(), user2.getUserId());

        assertThat(friendShipStorage.friendshipExists(user1.getUserId(), user2.getUserId())).isFalse();
    }

    @Test
    void testFriendshipExistsShouldReturnTrueWhenFriendshipExists() {
        User user1 = createTestUser("user1@test.com", "login1");
        User user2 = createTestUser("user2@test.com", "login2");
        friendShipStorage.addFriend(user1.getUserId(), user2.getUserId());
        boolean exists = friendShipStorage.friendshipExists(user1.getUserId(), user2.getUserId());

        assertThat(exists).isTrue();
    }

    @Test
    void testGetFriendsShouldReturnUserFriends() {
        User user1 = createTestUser("user1@test.com", "login1");
        User user2 = createTestUser("user2@test.com", "login2");
        User user3 = createTestUser("user3@test.com", "login3");
        friendShipStorage.addFriend(user1.getUserId(), user2.getUserId());
        friendShipStorage.addFriend(user1.getUserId(), user3.getUserId());
        List<User> friends = friendShipStorage.getFriends(user1.getUserId());

        assertThat(friends)
                .hasSize(2)
                .extracting(User::getUserId)
                .containsExactlyInAnyOrder(user2.getUserId(), user3.getUserId());
    }

    @Test
    void testGetCommonFriendsShouldReturnCommonFriends() {
        User user1 = createTestUser("user1@test.com", "login1");
        User user2 = createTestUser("user2@test.com", "login2");
        User commonFriend = createTestUser("common@test.com", "commonLogin");
        friendShipStorage.addFriend(user1.getUserId(), commonFriend.getUserId());
        friendShipStorage.addFriend(user2.getUserId(), commonFriend.getUserId());
        List<User> commonFriends = friendShipStorage.getCommonFriends(user1.getUserId(), user2.getUserId());

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getUserId)
                .containsExactly(commonFriend.getUserId());
    }
}
