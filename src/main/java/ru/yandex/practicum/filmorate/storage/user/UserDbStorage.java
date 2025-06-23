package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        setNameFromLoginIfEmpty(user);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", Date.valueOf(user.getBirthday()));

        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        log.info("Создан новый пользователь с ID: {}", id);

        return User.builder()
                .userId(id)
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    @Override
    public User updateUser(User user) {
        //todo Вынести в сервисный слой
        String checkSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        boolean exists = jdbcTemplate.queryForObject(checkSql, Long.class, user.getUserId()) > 0;
        if (!exists) {
            throw new NotFoundException("Пользователь с ID=" + user.getUserId() + " не найден");
        }

        setNameFromLoginIfEmpty(user);

        String sqlQuery = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getUserId());
        log.info("Пользователь с ID {} успешно обновлен", user.getUserId());

        updateFriendsInDatabase(user);

        return user;
    }

    @Override
    public List<User> findAllUsers() {
        String sqlQuery = "SELECT * FROM users";
        try {
            return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка пользователей", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Пользователь с ID {} не найден", userId);
            return Optional.empty();
        }
    }

    public List<User> findUsersByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();

        String sql = "SELECT * FROM users WHERE user_id IN (:ids)";
        Map<String, Object> params = Map.of("ids", ids);
        return namedJdbcTemplate.query(sql, params, this::mapRowToUser);
    }

    @Override
    public Map<Long, Set<Long>> getFriendships(Set<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();

        String sql = "SELECT user_id, friend_id FROM friendships WHERE user_id IN (:userIds)";
        Map<Long, Set<Long>> result = new HashMap<>();

        namedJdbcTemplate.query(sql, Map.of("userIds", userIds), rs -> {
            Long userId = rs.getLong("user_id");
            Long friendId = rs.getLong("friend_id");
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        });

        return result;
    }

    @Override
    public boolean friendshipExists(Long userId, Long friendId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM friendships WHERE user_id = ? AND friend_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, friendId));
    }

    private void setNameFromLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя {} установлено имя из логина", user.getLogin());
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        Long userId = rs.getLong("user_id");

        Set<Long> friends = new HashSet<>(
                jdbcTemplate.queryForList(
                        "SELECT friend_id FROM friendships WHERE user_id = ?",
                        Long.class,
                        userId
                )
        );

        return User.builder()
                .userId(userId)
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .friends(friends)
                .build();
    }

    private void updateFriendsInDatabase(User user) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ?", user.getUserId());

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
            List<Object[]> batchArgs = user.getFriends().stream()
                    .map(friendId -> new Object[]{user.getUserId(), friendId})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(insertSql, batchArgs);
        }
        log.info("Для пользователя с ID {} успешно обновлен список друзей {}", user.getUserId(), user.getFriends());
    }
}