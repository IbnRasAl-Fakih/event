-- 1) ENUM для статуса дружбы
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'friend_status') THEN
        CREATE TYPE friend_status AS ENUM ('pending', 'accepted', 'rejected', 'blocked');
    END IF;
END $$;

-- 2) Таблица friends
CREATE TABLE IF NOT EXISTS friends (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL,
    friend_user_id  UUID NOT NULL,
    status          friend_status NOT NULL DEFAULT 'pending',

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- нельзя дружить с самим собой
    CONSTRAINT chk_friends_not_self CHECK (user_id <> friend_user_id),

    -- FK
    CONSTRAINT fk_friends_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_friends_friend_user
        FOREIGN KEY (friend_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 3) Уникальность пары независимо от порядка (A-B == B-A)
-- требует расширение btree_gist (можно включить один раз)
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE friends
    ADD CONSTRAINT uq_friends_pair_unique
    EXCLUDE USING gist (
        LEAST(user_id, friend_user_id) WITH =,
        GREATEST(user_id, friend_user_id) WITH =
    );

-- 4) Индексы под запросы "мои друзья/заявки"
CREATE INDEX IF NOT EXISTS idx_friends_user_id
    ON friends (user_id);

CREATE INDEX IF NOT EXISTS idx_friends_friend_user_id
    ON friends (friend_user_id);

CREATE INDEX IF NOT EXISTS idx_friends_status
    ON friends (status);

-- 5) Триггер updated_at
DROP TRIGGER IF EXISTS tr_friends_updated_at ON friends;
CREATE TRIGGER tr_friends_updated_at
BEFORE UPDATE ON friends
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();