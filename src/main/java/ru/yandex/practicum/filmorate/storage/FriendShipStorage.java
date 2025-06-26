package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendShipStorage {

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    boolean friendshipExists(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long otherUserId);
}
