-- 1) Таблица post_likes
CREATE TABLE IF NOT EXISTS post_likes (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id    UUID NOT NULL,
    user_id    UUID NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- FK
    CONSTRAINT fk_post_likes_post
        FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 2) Нельзя лайкнуть один пост дважды
CREATE UNIQUE INDEX IF NOT EXISTS uq_post_likes_post_user
    ON post_likes (post_id, user_id);

-- 3) Индексы
CREATE INDEX IF NOT EXISTS idx_post_likes_post_id
    ON post_likes (post_id);

CREATE INDEX IF NOT EXISTS idx_post_likes_user_id
    ON post_likes (user_id);

CREATE INDEX IF NOT EXISTS idx_post_likes_created_at
    ON post_likes (created_at);