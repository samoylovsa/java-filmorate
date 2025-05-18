package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    UserStorage userStorage;

    @Autowired
    UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        User friend = userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        User friend = userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Set<Long> friendsIds = user.getFriends();

        if (friendsIds == null || friendsIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> friends = new ArrayList<>();
        for (Long friendId : friendsIds) {
            userStorage.findUserById(friendId)
                    .ifPresent(friends::add);
        }

        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new ValidationException("Нельзя искать общих друзей с самим собой");
        }

        User user = userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        User otherUser = userStorage.findUserById(otherUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + otherUserId + " не найден"));

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        if (userFriends.isEmpty() || otherUserFriends.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherUserFriends);

        List<User> commonFriends = commonFriendIds.stream()
                .map(userStorage::findUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return commonFriends;
    }
}