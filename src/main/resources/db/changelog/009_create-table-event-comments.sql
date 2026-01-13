-- 1) ENUM для типа комментария
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_comment_type') THEN
        CREATE TYPE event_comment_type AS ENUM ('text', 'photo', 'video', 'audio');
    END IF;
END $$;

-- 2) Таблица event_comments
CREATE TABLE IF NOT EXISTS event_comments (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id       UUID NOT NULL,
    author_user_id UUID NOT NULL,
    type           event_comment_type NOT NULL DEFAULT 'text',
    comment_text   TEXT,
    parent_id      UUID,

    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- FK
    CONSTRAINT fk_event_comments_event
        FOREIGN KEY (event_id)
        REFERENCES events (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_comments_author
        FOREIGN KEY (author_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    -- self-FK для ответов
    CONSTRAINT fk_event_comments_parent
        FOREIGN KEY (parent_id)
        REFERENCES event_comments (id)
        ON DELETE CASCADE,

    -- правила контента:
    -- для text комментарий обязателен, для остальных может быть null
    CONSTRAINT chk_event_comments_text_required_for_text_type
        CHECK (
            type <> 'text'
            OR (comment_text IS NOT NULL AND LENGTH(TRIM(comment_text)) > 0)
        )
);

-- 3) Индексы
CREATE INDEX IF NOT EXISTS idx_event_comments_event_id_created_at
    ON event_comments (event_id, created_at);

CREATE INDEX IF NOT EXISTS idx_event_comments_parent_id
    ON event_comments (parent_id);

CREATE INDEX IF NOT EXISTS idx_event_comments_author_user_id
    ON event_comments (author_user_id);

CREATE INDEX IF NOT EXISTS idx_event_comments_type
    ON event_comments (type);

-- 4) Триггер updated_at
DROP TRIGGER IF EXISTS tr_event_comments_updated_at ON event_comments;
CREATE TRIGGER tr_event_comments_updated_at
BEFORE UPDATE ON event_comments
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();