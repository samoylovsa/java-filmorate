package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    List<User> findAllUsers();

    Optional<User> findUserById(Long userId);
}
