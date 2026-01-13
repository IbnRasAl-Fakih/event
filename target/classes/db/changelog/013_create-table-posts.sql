-- 1) ENUM для статуса поста
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'post_status') THEN
        CREATE TYPE post_status AS ENUM ('active', 'draft', 'archived');
    END IF;
END $$;

-- 2) Таблица posts
CREATE TABLE IF NOT EXISTS posts (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_user_id UUID NOT NULL,
    event_id       UUID,
    text           TEXT,
    status         post_status NOT NULL DEFAULT 'draft',

    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- защита от пустого текста
    CONSTRAINT chk_posts_text_not_blank
        CHECK (LENGTH(TRIM(text)) > 0),

    -- FK
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_posts_event
        FOREIGN KEY (event_id)
        REFERENCES events (id)
        ON DELETE SET NULL
);

-- 3) Индексы
-- лента пользователя / профиля
CREATE INDEX IF NOT EXISTS idx_posts_author_created_at
    ON posts (author_user_id, created_at DESC);

-- лента ивента
CREATE INDEX IF NOT EXISTS idx_posts_event_created_at
    ON posts (event_id, created_at DESC);

-- фильтр по статусу
CREATE INDEX IF NOT EXISTS idx_posts_status
    ON posts (status);

-- 4) Триггер updated_at
DROP TRIGGER IF EXISTS tr_posts_updated_at ON posts;
CREATE TRIGGER tr_posts_updated_at
BEFORE UPDATE ON posts
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();