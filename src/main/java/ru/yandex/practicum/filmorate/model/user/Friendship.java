package ru.yandex.practicum.filmorate.model.user;

import lombok.Data;

@Data
public class Friendship {
    private Long userId;
    private Long friendId;
    private FriendshipStatus status = FriendshipStatus.PENDING;

    public void setStatus(String status) {
        this.status = FriendshipStatus.valueOf(status);
    }
}