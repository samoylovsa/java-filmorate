package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().minusYears(20));
    }

    @Test
    void createUserTest() {
        User user = userController.createUser(this.user);

        assertNotNull(user.getId());
        assertEquals("validLogin", user.getLogin());
    }

    @Test
    void createUserWithEmptyEmailTest() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Электронная почта не может быть пустой и должна содержать @",
                exception.getMessage());
    }

    @Test
    void createUserWithInvalidEmailFormatTest() {
        user.setEmail("invalid-email");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Электронная почта не может быть пустой и должна содержать @",
                exception.getMessage());
    }

    @Test
    void createUserWithEmptyLoginTest() {
        user.setLogin("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Логин не может быть пустым и содержать пробелы",
                exception.getMessage());
    }

    @Test
    void createUserWithLoginWithSpacesTest() {
        user.setLogin("login with spaces");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Логин не может быть пустым и содержать пробелы",
                exception.getMessage());
    }

    @Test
    void createUserWithFutureBirthdayTest() {
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));

        assertEquals("Дата рождения не может быть в будущем",
                exception.getMessage());
    }

    @Test
    void createUserWithNullNameTest() {
        user.setName(null);

        User created = userController.createUser(user);

        assertEquals("validLogin", created.getName());
    }

    @Test
    void updateUserWithNonExistingIdTest() {
        user.setId(999);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.updateUser(user));

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updateUserWithValidDataTest() {
        User created = userController.createUser(user);
        created.setEmail("new@example.com");

        User updated = userController.updateUser(created);

        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void getAllUsersTest() {
        userController.createUser(user);
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherLogin");
        anotherUser.setBirthday(LocalDate.now().minusYears(10));
        userController.createUser(anotherUser);

        assertEquals(2, userController.getAllUsers().size());
    }
}