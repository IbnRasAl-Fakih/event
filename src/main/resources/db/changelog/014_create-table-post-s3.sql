-- 1) ENUM для типа файла поста
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'post_file_type') THEN
        CREATE TYPE post_file_type AS ENUM ('photo', 'video', 'audio');
    END IF;
END $$;

-- 2) Таблица post_s3
CREATE TABLE IF NOT EXISTS post_s3 (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id     UUID NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 1,
    type        post_file_type NOT NULL,
    file_key    TEXT NOT NULL,

    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT chk_post_s3_file_key_not_blank
        CHECK (LENGTH(TRIM(file_key)) > 0),

    CONSTRAINT chk_post_s3_sort_order_positive
        CHECK (sort_order > 0),

    -- FK
    CONSTRAINT fk_post_s3_post
        FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON DELETE CASCADE
);

-- 3) Уникальность порядка файлов внутри поста
CREATE UNIQUE INDEX IF NOT EXISTS uq_post_s3_post_sort
    ON post_s3 (post_id, sort_order);

-- 4) Уникальность file_key внутри поста
CREATE UNIQUE INDEX IF NOT EXISTS uq_post_s3_post_file_key
    ON post_s3 (post_id, file_key);

-- 5) Индексы
CREATE INDEX IF NOT EXISTS idx_post_s3_post_id
    ON post_s3 (post_id);

CREATE INDEX IF NOT EXISTS idx_post_s3_type
    ON post_s3 (type);

CREATE INDEX IF NOT EXISTS idx_post_s3_created_at
    ON post_s3 (created_at);

-- 6) Триггер updated_at
DROP TRIGGER IF EXISTS tr_post_s3_updated_at ON post_s3;
CREATE TRIGGER tr_post_s3_updated_at
BEFORE UPDATE ON post_s3
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();