package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Long idCounter = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        try {
            validateUser(user);
            user.setId(idCounter++);
            setNameFromLoginIfEmpty(user);
            users.put(user.getId(), user);
            log.info("Создан новый пользователь с ID: {}", user.getId());
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            if (user.getId() == null || !users.containsKey(user.getId())) {
                throw new ValidationException("Пользователь не найден");
            }
            validateUser(user);
            setNameFromLoginIfEmpty(user);
            users.put(user.getId(), user);
            log.info("Пользователь с ID {} успешно обновлен", user.getId());
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private void validateUser(User user) {
        log.debug("Начало валидации пользователя: {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Некорректный email пользователя: {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Некорректный логин пользователя: '{}'", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения пользователя в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация пользователя пройдена успешно");
    }

    private void setNameFromLoginIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя {} установлено имя из логина", user.getLogin());
        }
    }
}
