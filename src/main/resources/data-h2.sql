-- Заполнение таблицы рейтингов
MERGE INTO ratings (name, description) KEY(name) VALUES
('G', 'Нет возрастных ограничений'),
('PG', 'Рекомендуется присутствие родителей'),
('PG-13', 'Детям до 13 лет просмотр не желателен'),
('R', 'Лицам до 17 лет обязательно присутствие взрослого'),
('NC-17', 'Лицам до 18 лет просмотр запрещен');

-- Заполнение таблицы жанров
MERGE INTO genres (name) KEY(name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

-- Заполнение таблицы пользователей
MERGE INTO users (email, login, name, birthday) KEY(email) VALUES
('user1@example.com', 'user1', 'John Doe', '1990-05-15'),
('user2@example.com', 'user2', 'Jane Smith', '1985-08-21'),
('user3@example.com', 'user3', 'Mike Johnson', '1995-03-10'),
('user4@example.com', 'user4', 'Emily Davis', '2000-11-30'),
('admin@example.com', 'admin', 'Admin', '1980-01-01');

-- Заполнение таблицы фильмов (исправлены ссылки на таблицу ratings)
MERGE INTO films (name, description, release_date, duration, rating_id) KEY(name) VALUES
('The Matrix', 'Научно-фантастический боевик', '1999-03-31', 136,
  (SELECT rating_id FROM ratings WHERE name = 'R')),
('Forrest Gump', 'История жизни Форреста Гампа', '1994-07-06', 142,
  (SELECT rating_id FROM ratings WHERE name = 'PG-13')),
('Inception', 'Фильм о проникновении в сны', '2010-07-16', 148,
  (SELECT rating_id FROM ratings WHERE name = 'PG-13')),
('The Shawshank Redemption', 'Драма о жизни в тюрьме', '1994-09-23', 142,
  (SELECT rating_id FROM ratings WHERE name = 'R')),
('Toy Story', 'История игрушек', '1995-11-22', 81,
  (SELECT rating_id FROM ratings WHERE name = 'G'));

-- Заполнение таблицы связи фильмов и жанров (исправлены ссылки на таблицы films и genres)
MERGE INTO film_genre (film_id, genre_id) KEY(film_id, genre_id)
SELECT f.film_id, g.genre_id
FROM (VALUES
    ('The Matrix', 'Боевик'),
    ('The Matrix', 'Триллер'),
    ('Forrest Gump', 'Драма'),
    ('Inception', 'Триллер'),
    ('Inception', 'Боевик'),
    ('The Shawshank Redemption', 'Драма'),
    ('Toy Story', 'Мультфильм'),
    ('Toy Story', 'Комедия')
) AS data(film_name, genre_name)
JOIN films f ON f.name = data.film_name
JOIN genres g ON g.name = data.genre_name;

-- Заполнение таблицы лайков (исправлены ссылки на таблицы films и users)
MERGE INTO likes (film_id, user_id) KEY(film_id, user_id)
SELECT f.film_id, u.user_id
FROM (VALUES
    ('The Matrix', 'user1@example.com'),
    ('The Matrix', 'user2@example.com'),
    ('The Matrix', 'user3@example.com'),
    ('Forrest Gump', 'user1@example.com'),
    ('Forrest Gump', 'user4@example.com'),
    ('Inception', 'user2@example.com'),
    ('Inception', 'user3@example.com'),
    ('Inception', 'admin@example.com'),
    ('Toy Story', 'user1@example.com'),
    ('Toy Story', 'user2@example.com'),
    ('Toy Story', 'user3@example.com'),
    ('Toy Story', 'user4@example.com')
) AS data(film_name, user_email)
JOIN films f ON f.name = data.film_name
JOIN users u ON u.email = data.user_email;

-- Заполнение таблицы дружбы (исправлены ссылки на таблицу users)
MERGE INTO friendships (user_id, friend_id) KEY(user_id, friend_id)
SELECT u1.user_id, u2.user_id
FROM (VALUES
    ('user1@example.com', 'user2@example.com'),
    ('user1@example.com', 'user3@example.com'),
    ('user2@example.com', 'user3@example.com'),
    ('user4@example.com', 'user1@example.com'),
    ('admin@example.com', 'user2@example.com')
) AS data(user_email, friend_email)
JOIN users u1 ON u1.email = data.user_email
JOIN users u2 ON u2.email = data.friend_email;