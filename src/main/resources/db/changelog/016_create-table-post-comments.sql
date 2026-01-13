-- 1) ENUM для типа комментария к посту
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'post_comment_type') THEN
        CREATE TYPE post_comment_type AS ENUM ('text', 'photo', 'video', 'audio');
    END IF;
END $$;

-- 2) Таблица post_comments
CREATE TABLE IF NOT EXISTS post_comments (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id        UUID NOT NULL,
    author_user_id UUID NOT NULL,
    type           post_comment_type NOT NULL DEFAULT 'text',
    comment_text   TEXT,
    parent_id      UUID,

    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- FK
    CONSTRAINT fk_post_comments_post
        FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_post_comments_author
        FOREIGN KEY (author_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    -- self-FK для ответов
    CONSTRAINT fk_post_comments_parent
        FOREIGN KEY (parent_id)
        REFERENCES post_comments (id)
        ON DELETE CASCADE,

    -- если type=text, то текст обязателен
    CONSTRAINT chk_post_comments_text_required_for_text_type
        CHECK (
            type <> 'text'
            OR (comment_text IS NOT NULL AND LENGTH(TRIM(comment_text)) > 0)
        )
);

-- 3) Индексы
CREATE INDEX IF NOT EXISTS idx_post_comments_post_id_created_at
    ON post_comments (post_id, created_at);

CREATE INDEX IF NOT EXISTS idx_post_comments_parent_id
    ON post_comments (parent_id);

CREATE INDEX IF NOT EXISTS idx_post_comments_author_user_id
    ON post_comments (author_user_id);

CREATE INDEX IF NOT EXISTS idx_post_comments_type
    ON post_comments (type);

-- 4) Триггер updated_at
DROP TRIGGER IF EXISTS tr_post_comments_updated_at ON post_comments;
CREATE TRIGGER tr_post_comments_updated_at
BEFORE UPDATE ON post_comments
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();