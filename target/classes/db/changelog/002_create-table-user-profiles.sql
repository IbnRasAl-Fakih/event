-- 1. Создаем таблицу user_profiles
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id        UUID PRIMARY KEY,
    username       VARCHAR(100) UNIQUE,
    full_name      VARCHAR(255),
    avatar_key     TEXT,
    bio            TEXT,
    job            VARCHAR(255),
    city           VARCHAR(100),
    birthdate      DATE,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 2. Индексы (поиск / профиль)
CREATE INDEX IF NOT EXISTS idx_user_profiles_username
    ON user_profiles (username);

CREATE INDEX IF NOT EXISTS idx_user_profiles_city
    ON user_profiles (city);

-- 3. Триггер updated_at
DROP TRIGGER IF EXISTS tr_user_profiles_updated_at ON user_profiles;
CREATE TRIGGER tr_user_profiles_updated_at
BEFORE UPDATE ON user_profiles
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();