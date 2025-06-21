# Схема базы данных Filmorate
## Интерактивная версия схемы
Схему можно посмотреть на MermaidChart:  
[🔗 Открыть схему в MermaidChart](https://www.mermaidchart.com/app/projects/0eb7f74b-3f50-4511-9949-4285164fdea2/diagrams/8057e53d-d9e1-463b-8184-42ac54326b03)

## Примеры SQL запросов к базе данных

### Пользователи
#### Добавить пользователя
```sql
INSERT INTO "user" (email, login, name, birthday)
VALUES ('example@mail.com', 'user123', 'Иван Иванов', '1990-05-15');
```

#### Обновить данные пользователя
```sql
UPDATE "user"
SET name = 'Новое имя', birthday = '1995-03-20'
WHERE user_id = 1;
```

#### Получить всех пользователей
```sql
SELECT user_id, email, login, name, birthday 
FROM "user"
ORDER BY user_id;
```

#### Добавить запрос на дружбу
```sql
INSERT INTO friendship (user_id, friend_id, status)
VALUES (1, 2, 'PENDING');
```

#### Подтвердить дружбу
```sql
UPDATE friendship
SET status = 'CONFIRMED'
WHERE user_id = 2 AND friend_id = 1;
```

#### Удалить друга
```sql
DELETE FROM friendship
WHERE (user_id = 1 AND friend_id = 2)
   OR (user_id = 2 AND friend_id = 1);
```

#### Получить друзей пользователя
```sql
SELECT u.user_id, u.login, u.name
FROM friendship f
JOIN "user" u ON f.friend_id = u.user_id
WHERE f.user_id = 1 AND f.status = 'CONFIRMED';
```

#### Получить общих друзей
```sql
SELECT u.user_id, u.login, u.name
FROM friendship f1
JOIN friendship f2 ON f1.friend_id = f2.friend_id
JOIN "user" u ON f1.friend_id = u.user_id
WHERE f1.user_id = 1 AND f2.user_id = 2
  AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED';
```

### Фильмы
#### Добавить фильм
```sql
INSERT INTO film (name, description, release_date, duration, rating_id)
VALUES ('Интерстеллар', 'Фантастический фильм о космосе', '2014-11-06', 169, 3);
```

#### Обновить данные фильма
```sql
UPDATE film
SET description = 'Новое описание фильма', duration = 175
WHERE film_id = 1;
```

#### Получить все фильмы
```sql
SELECT f.film_id, f.name, f.release_date, r.name AS rating
FROM film f
JOIN rating r ON f.rating_id = r.rating_id
ORDER BY f.film_id;
```

#### Добавить жанры фильму
```sql
INSERT INTO film_genre (film_id, genre_id)
VALUES (1, 1), (1, 3); -- Комедия и Мультфильм
```

#### Поставить лайк
```sql
INSERT INTO like (film_id, user_id)
VALUES (1, 5);
```

#### Удалить лайк
```sql
DELETE FROM like
WHERE film_id = 1 AND user_id = 5;
```

#### Топ-10 популярных фильмов
```sql
SELECT f.film_id, f.name, COUNT(l.user_id) AS likes
FROM film f
LEFT JOIN like l ON f.film_id = l.film_id
GROUP BY f.film_id
ORDER BY likes DESC
LIMIT 10;
```

#### Получить лайки для фильма
```sql
SELECT u.user_id, u.login, u.name
FROM like l
JOIN "user" u ON l.user_id = u.user_id
WHERE l.film_id = 1;
```

#### Встроенная диаграмма
```mermaid
erDiagram
    user ||--o{ friendship : "1:N (user-friend)"
    user ||--o{ like : "1:N (user-like)"
    film ||--o{ like : "1:N (film-like)"
    film ||--o{ film_genre : "1:N"
    genre ||--o{ film_genre : "1:N"
    film }|--|| rating : "N:1"
    
    user {
        bigint user_id PK
        varchar(255) email
        varchar(255) login
        varchar(255) name
        date birthday
    }
    
    film {
        bigint film_id PK
        varchar(255) name
        text description
        date release_date
        integer duration
        integer rating_id FK
    }
    
    rating {
        integer rating_id PK
        varchar(50) name
        varchar(255) description
    }
    
    genre {
        integer genre_id PK
        varchar(50) name
    }
    
    film_genre {
        bigint film_id PK,FK
        integer genre_id PK,FK
    }
    
    like {
        bigint film_id PK,FK
        bigint user_id PK,FK
    }
    
    friendship {
        bigint user_id PK,FK
        bigint friend_id PK,FK
        varchar(20) status
    }