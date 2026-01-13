-- 1) ENUM для типа файла комментария
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_comment_file_type') THEN
        CREATE TYPE event_comment_file_type AS ENUM ('photo', 'video', 'audio');
    END IF;
END $$;

-- 2) Таблица event_comment_s3
CREATE TABLE IF NOT EXISTS event_comment_s3 (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_id  UUID NOT NULL,
    type        event_comment_file_type NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 1,
    file_key    TEXT NOT NULL,

    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- базовая защита
    CONSTRAINT chk_event_comment_s3_file_key_not_blank
        CHECK (LENGTH(TRIM(file_key)) > 0),

    CONSTRAINT chk_event_comment_s3_sort_order_positive
        CHECK (sort_order > 0),

    -- FK
    CONSTRAINT fk_event_comment_s3_comment
        FOREIGN KEY (comment_id)
        REFERENCES event_comments (id)
        ON DELETE CASCADE
);

-- 3) Уникальность порядка файлов внутри комментария
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_comment_s3_comment_sort
    ON event_comment_s3 (comment_id, sort_order);

-- 4) Уникальность file_key внутри комментария (чтобы не дублировать один и тот же файл)
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_comment_s3_comment_file_key
    ON event_comment_s3 (comment_id, file_key);

-- 5) Индексы
CREATE INDEX IF NOT EXISTS idx_event_comment_s3_comment_id
    ON event_comment_s3 (comment_id);

CREATE INDEX IF NOT EXISTS idx_event_comment_s3_type
    ON event_comment_s3 (type);

CREATE INDEX IF NOT EXISTS idx_event_comment_s3_created_at
    ON event_comment_s3 (created_at);

-- 6) Триггер updated_at
DROP TRIGGER IF EXISTS tr_event_comment_s3_updated_at ON event_comment_s3;
CREATE TRIGGER tr_event_comment_s3_updated_at
BEFORE UPDATE ON event_comment_s3
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();