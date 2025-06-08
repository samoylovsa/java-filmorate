-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS "user" (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE,
    CONSTRAINT uc_user_email UNIQUE (email),
    CONSTRAINT uc_user_login UNIQUE (login)
);

-- Создание таблицы рейтингов
CREATE TABLE IF NOT EXISTS rating (
    rating_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uc_rating_name UNIQUE (name)
);

-- Создание таблицы фильмов
CREATE TABLE IF NOT EXISTS film (
    film_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INTEGER,
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES rating(rating_id)
);

-- Создание таблицы жанров
CREATE TABLE IF NOT EXISTS genre (
    genre_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uc_genre_name UNIQUE (name)
);

-- Создание таблицы связи фильмов и жанров (многие-ко-многим)
CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT,
    genre_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id) ON DELETE CASCADE
);

-- Создание таблицы лайков (пользователи -> фильмы)
CREATE TABLE IF NOT EXISTS "like" (
    film_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE
);

-- Создание таблицы дружбы между пользователями
CREATE TABLE IF NOT EXISTS friendship (
    user_id BIGINT,
    friend_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CHECK (user_id <> friend_id)
);

-- Вставка начальных данных для рейтингов
INSERT INTO rating (name, description) VALUES
('G', 'Нет возрастных ограничений'),
('PG', 'Рекомендуется присутствие родителей'),
('PG-13', 'Детям до 13 лет просмотр не желателен'),
('R', 'Лицам до 17 лет обязательно присутствие взрослого'),
('NC-17', 'Лицам до 18 лет просмотр запрещен')
ON CONFLICT (name) DO NOTHING;

-- Вставка начальных данных для жанров
INSERT INTO genre (name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик')
ON CONFLICT (name) DO NOTHING;