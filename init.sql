CREATE TABLE URLS
(
    id       SERIAL PRIMARY KEY,
    url_key  VARCHAR(255) UNIQUE NOT NULL,
    full_url VARCHAR(255) UNIQUE NOT NULL
);