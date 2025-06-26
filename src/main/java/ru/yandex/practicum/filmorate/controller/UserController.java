package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.request.UserRequest;
import ru.yandex.practicum.filmorate.dto.response.UserResponse;
import ru.yandex.practicum.filmorate.service.FriendShipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FriendShipService friendShipService;

    @PostMapping
    public UserResponse addUser(@RequestBody UserRequest userRequest) {
        log.info("Получен запрос на создание пользователя: {}", userRequest);
        return userService.addUser(userRequest);
    }

    @PutMapping
    public UserResponse updateUser(@RequestBody UserRequest userRequest) {
        log.info("Получен запрос на обновление пользователя с ID: {}", userRequest.getId());
        return userService.updateUser(userRequest);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        log.info("Получен запрос на список всех пользователей.");
        return userService.findAllUsers();
    }

    @PutMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId
    ) {
        log.info("Получен запрос на добавление пользователя в друзья");
        friendShipService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {
        log.info("Получен запрос на удаление друга с ID: {} для пользователя с ID: {}", friendId, id);
        friendShipService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserResponse> getFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение списка друзей");
        return friendShipService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserResponse> getCommonFriends(
            @PathVariable Long id,
            @PathVariable Long otherId
    ) {
        log.info("Получен запрос на получение списка общих друзей");
        return friendShipService.getCommonFriends(id, otherId);
    }
}