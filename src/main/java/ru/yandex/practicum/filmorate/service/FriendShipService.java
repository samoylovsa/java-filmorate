package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.response.UserResponse;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendShipStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendShipService {

    private final UserService userService;
    private final FriendShipStorage friendShipStorage;

    public void addFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);
        userService.validateUserExist(userId);
        userService.validateUserExist(friendId);
        validateUsersAlreadyFriends(userId, friendId);
        friendShipStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);
        userService.validateUserExist(userId);
        userService.validateUserExist(friendId);
        if (!friendShipStorage.friendshipExists(userId, friendId)) {
            // Для теста "Not friend remove" - просто возвращаемся без ошибки
            return;
        }
        friendShipStorage.deleteFriend(userId, friendId);
    }

    public List<UserResponse> getFriends(Long userId) {
        userService.validateUserExist(userId);
        List<User> users = friendShipStorage.getFriends(userId);

        return UserMapper.mapToListOfUserResponses(users);
    }

    public List<UserResponse> getCommonFriends(Long userId, Long otherUserId) {
        validateNotSameUser(userId, otherUserId);
        userService.validateUserExist(userId);
        userService.validateUserExist(otherUserId);
        List<User> users = friendShipStorage.getCommonFriends(userId, otherUserId);

        return UserMapper.mapToListOfUserResponses(users);
    }

    private void validateNotSameUser(Long firstUser, Long secondUser) {
        if (firstUser.equals(secondUser)) {
            throw new ValidationException("Нельзя выполнить операцию с самим собой");
        }
    }

    private void validateUsersAlreadyFriends(Long userId, Long friendId) {
        boolean isUsersAlreadyFriends = friendShipStorage.friendshipExists(userId, friendId);
        if (isUsersAlreadyFriends) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }
    }
}
