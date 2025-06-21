package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Long idCounter = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        user.setUserId(idCounter++);
        setNameFromLoginIfEmpty(user);
        users.put(user.getUserId(), user);
        log.info("Создан новый пользователь с ID: {}", user.getUserId());

        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getUserId() == null || !users.containsKey(user.getUserId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        setNameFromLoginIfEmpty(user);
        users.put(user.getUserId(), user);
        log.info("Пользователь с ID {} успешно обновлен", user.getUserId());

        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private void setNameFromLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя {} установлено имя из логина", user.getLogin());
        }
    }
}
