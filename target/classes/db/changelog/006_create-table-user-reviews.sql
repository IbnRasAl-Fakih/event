-- 1) Таблица user_reviews
CREATE TABLE IF NOT EXISTS user_reviews (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_user_id     UUID NOT NULL,
    recipient_user_id  UUID NOT NULL,
    event_id           UUID NOT NULL,
    text               TEXT,
    rating             SMALLINT NOT NULL,

    created_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- нельзя оставить отзыв самому себе
    CONSTRAINT chk_user_reviews_not_self
        CHECK (author_user_id <> recipient_user_id),

    -- рейтинг 0..5
    CONSTRAINT chk_user_reviews_rating_range
        CHECK (rating BETWEEN 0 AND 5),

    -- FK
    CONSTRAINT fk_user_reviews_author
        FOREIGN KEY (author_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_reviews_recipient
        FOREIGN KEY (recipient_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_reviews_event
        FOREIGN KEY (event_id)
        REFERENCES events (id)
        ON DELETE CASCADE
);

-- 2) Один отзыв:
-- автор → получатель → ивент
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_reviews_author_recipient_event
    ON user_reviews (author_user_id, recipient_user_id, event_id);

-- 3) Индексы под основные запросы
CREATE INDEX IF NOT EXISTS idx_user_reviews_author
    ON user_reviews (author_user_id);

CREATE INDEX IF NOT EXISTS idx_user_reviews_recipient
    ON user_reviews (recipient_user_id);

CREATE INDEX IF NOT EXISTS idx_user_reviews_event
    ON user_reviews (event_id);

CREATE INDEX IF NOT EXISTS idx_user_reviews_created_at
    ON user_reviews (created_at);

-- 4) Триггер updated_at
DROP TRIGGER IF EXISTS tr_user_reviews_updated_at ON user_reviews;
CREATE TRIGGER tr_user_reviews_updated_at
BEFORE UPDATE ON user_reviews
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();