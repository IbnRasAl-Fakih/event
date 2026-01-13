-- 1) Таблица user_socials (без ENUM)
CREATE TABLE IF NOT EXISTS user_socials (
    user_id     UUID NOT NULL,
    type        VARCHAR(50) NOT NULL,
    url_text    TEXT NOT NULL,

    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_user_socials_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    -- базовая защита от пустых значений
    CONSTRAINT chk_user_socials_type_not_blank
        CHECK (LENGTH(TRIM(type)) > 0),

    CONSTRAINT chk_user_socials_url_not_blank
        CHECK (LENGTH(TRIM(url_text)) > 0)
);

-- 2) Уникальность: один тип соцсети на пользователя
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_socials_user_type
    ON user_socials (user_id, type);

-- 3) Индексы для выборок
CREATE INDEX IF NOT EXISTS idx_user_socials_user_id
    ON user_socials (user_id);

CREATE INDEX IF NOT EXISTS idx_user_socials_type
    ON user_socials (type);

-- 4) Триггер updated_at
DROP TRIGGER IF EXISTS tr_user_socials_updated_at ON user_socials;
CREATE TRIGGER tr_user_socials_updated_at
BEFORE UPDATE ON user_socials
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();