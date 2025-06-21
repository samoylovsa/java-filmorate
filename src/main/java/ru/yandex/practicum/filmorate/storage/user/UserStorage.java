package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.*;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    List<User> findAllUsers();

    Optional<User> findUserById(Long userId);

    List<User> findUsersByIds(Collection<Long> ids);

    Map<Long, Set<Long>> getFriendships(Set<Long> userIds);
}
