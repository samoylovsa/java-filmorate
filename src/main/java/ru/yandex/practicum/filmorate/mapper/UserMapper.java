package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.request.UserRequest;
import ru.yandex.practicum.filmorate.dto.response.UserResponse;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User mapToUser(UserRequest userRequest) {
        return User.builder()
                .userId(userRequest.getId())
                .email(userRequest.getEmail())
                .login(userRequest.getLogin())
                .name(userRequest.getName())
                .birthday(userRequest.getBirthday())
                .build();
    }

    public static UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public static List<UserResponse> mapToListOfUserResponses(List<User> users) {
        List<UserResponse> userResponses = new ArrayList<>(users.size());
        for (User user : users) {
            userResponses.add(UserMapper.mapToUserResponse(user));
        }
        return userResponses;
    }
}
