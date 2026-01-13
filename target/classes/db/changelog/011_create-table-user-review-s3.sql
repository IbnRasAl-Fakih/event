-- 1) ENUM для типа файла отзыва
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_review_file_type') THEN
        CREATE TYPE user_review_file_type AS ENUM ('photo', 'video', 'audio');
    END IF;
END $$;

-- 2) Таблица user_review_s3
CREATE TABLE IF NOT EXISTS user_review_s3 (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_review_id UUID NOT NULL,
    type           user_review_file_type NOT NULL,
    sort_order     INTEGER NOT NULL DEFAULT 1,
    file_key       TEXT NOT NULL,

    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- базовая защита
    CONSTRAINT chk_user_review_s3_file_key_not_blank
        CHECK (LENGTH(TRIM(file_key)) > 0),

    CONSTRAINT chk_user_review_s3_sort_order_positive
        CHECK (sort_order > 0),

    -- FK
    CONSTRAINT fk_user_review_s3_review
        FOREIGN KEY (user_review_id)
        REFERENCES user_reviews (id)
        ON DELETE CASCADE
);

-- 3) Уникальность порядка внутри одного отзыва
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_review_s3_review_sort
    ON user_review_s3 (user_review_id, sort_order);

-- 4) Уникальность file_key внутри одного отзыва (не дублировать один и тот же файл)
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_review_s3_review_file_key
    ON user_review_s3 (user_review_id, file_key);

-- 5) Индексы
CREATE INDEX IF NOT EXISTS idx_user_review_s3_user_review_id
    ON user_review_s3 (user_review_id);

CREATE INDEX IF NOT EXISTS idx_user_review_s3_type
    ON user_review_s3 (type);

CREATE INDEX IF NOT EXISTS idx_user_review_s3_created_at
    ON user_review_s3 (created_at);

-- 6) Триггер updated_at
DROP TRIGGER IF EXISTS tr_user_review_s3_updated_at ON user_review_s3;
CREATE TRIGGER tr_user_review_s3_updated_at
BEFORE UPDATE ON user_review_s3
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();