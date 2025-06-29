package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendShipDbStorage implements FriendShipStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final RowMapper<User> userMapper;

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.debug("Добавлена дружба: {} -> {}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.debug("Удалена дружба: {} -> {}", userId, friendId);
    }

    @Override
    public boolean friendshipExists(Long userId, Long friendId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM friendships WHERE user_id = ? AND friend_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, friendId));
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
                SELECT u.* FROM users u
                JOIN friendships f ON u.user_id = f.friend_id
                WHERE f.user_id = ?
                """;
        log.debug("Получение друзей пользователя ID: {}", userId);
        List<User> friends = jdbcTemplate.query(sql, userMapper, userId);
        log.debug("Получены друзья пользователя: {}", friends);
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = """
        SELECT u.* FROM users u
        JOIN friendships f1 ON u.user_id = f1.friend_id AND f1.user_id = ?
        JOIN friendships f2 ON u.user_id = f2.friend_id AND f2.user_id = ?
        """;
        log.debug("Запрос общих друзей для пользователей {} и {}", userId, otherUserId);
        List<User> friends = jdbcTemplate.query(sql, userMapper, userId, otherUserId);
        log.debug("Результат поиска общих друзей: найдено {} пользователей", friends.size());
        return friends;
    }
}
