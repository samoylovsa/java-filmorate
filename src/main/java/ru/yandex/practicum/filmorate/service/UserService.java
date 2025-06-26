package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.request.UserRequest;
import ru.yandex.practicum.filmorate.dto.response.UserResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public UserResponse addUser(UserRequest userRequest) {
        validateUser(userRequest);
        setNameFromLoginIfEmpty(userRequest);
        User user = UserMapper.mapToUser(userRequest);
        user = userStorage.addUser(user);

        return UserMapper.mapToUserResponse(user);
    }

    public UserResponse updateUser(UserRequest userRequest) {
        validateUser(userRequest);
        setNameFromLoginIfEmpty(userRequest);
        validateUserExist(userRequest.getId());
        User user = UserMapper.mapToUser(userRequest);
        user = userStorage.updateUser(user);

        return UserMapper.mapToUserResponse(user);
    }

    public List<UserResponse> findAllUsers() {
        List<User> users = userStorage.findAllUsers();

        return UserMapper.mapToListOfUserResponses(users);
    }

    public void validateUserExist(Long userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateUser(UserRequest userRequest) {
        log.debug("Начало валидации пользователя: {}", userRequest);
        if (userRequest.getEmail() == null || userRequest.getEmail().isBlank() || !userRequest.getEmail().contains("@")) {
            log.error("Некорректный email пользователя: {}", userRequest.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать @");
        }

        if (userRequest.getLogin() == null || userRequest.getLogin().isBlank() || userRequest.getLogin().contains(" ")) {
            log.error("Некорректный логин пользователя: '{}'", userRequest.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (userRequest.getBirthday() != null && userRequest.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения пользователя в будущем: {}", userRequest.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация пользователя пройдена успешно");
    }

    private void setNameFromLoginIfEmpty(UserRequest userRequest) {
        if (userRequest.getName() == null || userRequest.getName().isBlank()) {
            userRequest.setName(userRequest.getLogin());
            log.debug("Для пользователя {} установлено имя из логина", userRequest.getLogin());
        }
    }
}