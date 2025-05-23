package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        validateUser(user);

        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        validateUser(user);

        return userStorage.updateUser(user);
    }

    public List<User> findAllUsers() {
        return userStorage.findAllUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);

        User user = findUserById(userId);
        User friend = findUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        addMutualFriendship(user, friend);

        saveUsers(user, friend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);

        User user = findUserById(userId);
        User friend = findUserById(friendId);

        removeMutualFriendship(user, friend);

        saveUsers(user, friend);
    }

    public List<User> getFriends(Long userId) {
        User user = findUserById(userId);

        return getFriendsList(user.getFriends());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        validateNotSameUser(userId, otherUserId);

        User user = findUserById(userId);
        User otherUser = findUserById(otherUserId);

        return findCommonFriends(user.getFriends(), otherUser.getFriends());
    }

    private void validateNotSameUser(Long firstUser, Long secondUser) {
        if (firstUser.equals(secondUser)) {
            throw new ValidationException("Нельзя выполнить операцию с самим собой");
        }
    }

    private User findUserById(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

    }

    private void addMutualFriendship(User user, User friend) {
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
    }

    private void removeMutualFriendship(User user, User friend) {
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
    }

    private void saveUsers(User... users) {
        for (User user : users) {
            userStorage.updateUser(user);
        }
    }

    private List<User> getFriendsList(Set<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return Collections.emptyList();
        }

        return friendIds.stream()
                .map(userStorage::findUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<User> findCommonFriends(Set<Long> friends1, Set<Long> friends2) {
        if (friends1.isEmpty() || friends2.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> commonIds = new HashSet<>(friends1);
        commonIds.retainAll(friends2);

        return getFriendsList(commonIds);
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
}