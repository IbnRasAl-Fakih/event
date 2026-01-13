-- 1) ENUM для типа файла ивента
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_file_type') THEN
        CREATE TYPE event_file_type AS ENUM ('photo', 'video');
    END IF;
END $$;

-- 2) Таблица event_s3
CREATE TABLE IF NOT EXISTS event_s3 (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id        UUID NOT NULL,
    author_user_id  UUID NOT NULL,
    type            event_file_type NOT NULL,
    file_key        TEXT NOT NULL,

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- базовая защита
    CONSTRAINT chk_event_s3_file_key_not_blank
        CHECK (LENGTH(TRIM(file_key)) > 0),

    -- FK
    CONSTRAINT fk_event_s3_event
        FOREIGN KEY (event_id)
        REFERENCES events (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_s3_author
        FOREIGN KEY (author_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 3) Уникальность: один и тот же файл не дублируется в рамках ивента
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_s3_event_file_key
    ON event_s3 (event_id, file_key);

-- 4) Индексы
CREATE INDEX IF NOT EXISTS idx_event_s3_event_id
    ON event_s3 (event_id);

CREATE INDEX IF NOT EXISTS idx_event_s3_type
    ON event_s3 (type);

CREATE INDEX IF NOT EXISTS idx_event_s3_author_user_id
    ON event_s3 (author_user_id);

CREATE INDEX IF NOT EXISTS idx_event_s3_created_at
    ON event_s3 (created_at);

-- 5) Триггер updated_at
DROP TRIGGER IF EXISTS tr_event_s3_updated_at ON event_s3;
CREATE TRIGGER tr_event_s3_updated_at
BEFORE UPDATE ON event_s3
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();