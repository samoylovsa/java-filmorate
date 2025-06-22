package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class UserService {

    private UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
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

        user.getFriends().add(friend.getUserId());

        userStorage.updateUser(user);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validateNotSameUser(userId, friendId);

        User user = findUserById(userId);
        findUserById(friendId);

        if (!userStorage.friendshipExists(userId, friendId)) {
            // Для теста "Not friend remove" - просто возвращаемся без ошибки
            return;
        }

        Set<Long> updatedFriends = user.getFriends() != null ?
                new HashSet<>(user.getFriends()) : new HashSet<>();

        updatedFriends.remove(friendId);

        if (user.getFriends() == null || !user.getFriends().equals(updatedFriends)) {
            User updatedUser = User.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .login(user.getLogin())
                    .name(user.getName())
                    .birthday(user.getBirthday())
                    .friends(updatedFriends)
                    .build();

            userStorage.updateUser(updatedUser);
        }
    }

    public List<User> getFriends(Long userId) {
        User user = findUserById(userId);
        Set<Long> friends = user.getFriends();

        if (friends == null || friends.isEmpty()) {
            return Collections.emptyList();
        }

        return userStorage.findUsersByIds(friends);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        validateNotSameUser(userId, otherUserId);

        Map<Long, Set<Long>> friendsMap = userStorage.getFriendships(Set.of(userId, otherUserId));

        Set<Long> userFriends = friendsMap.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherUserFriends = friendsMap.getOrDefault(otherUserId, Collections.emptySet());

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherUserFriends);

        return userStorage.findUsersByIds(commonIds);
    }

    private User findUserById(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateNotSameUser(Long firstUser, Long secondUser) {
        if (firstUser.equals(secondUser)) {
            throw new ValidationException("Нельзя выполнить операцию с самим собой");
        }
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