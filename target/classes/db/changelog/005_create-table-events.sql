-- 1) ENUM'ы для events
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_visibility') THEN
        CREATE TYPE event_visibility AS ENUM ('public', 'private');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_status') THEN
        CREATE TYPE event_status AS ENUM ('draft', 'published', 'cancelled', 'archived');
    END IF;
END $$;

-- 2) Таблица events
CREATE TABLE IF NOT EXISTS events (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    creator_user_id UUID NOT NULL,
    title           VARCHAR(255) NOT NULL,
    slogan          VARCHAR(255),
    description     TEXT,
    date            DATE NOT NULL,
    time            VARCHAR(100) NOT NULL,
    duration_minutes INTEGER,
    location        VARCHAR(255),
    price           NUMERIC(10,2) DEFAULT 0,
    capacity        INTEGER,
    min_rating      SMALLINT,
    visibility      event_visibility NOT NULL DEFAULT 'public',
    status          event_status NOT NULL DEFAULT 'draft',
    tags            TEXT[],
    cover_image_url TEXT,
    slider_images   TEXT[],

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- ограничения
    CONSTRAINT chk_events_price_non_negative
        CHECK (price >= 0),

    CONSTRAINT chk_events_capacity_positive
        CHECK (capacity IS NULL OR capacity > 0),

    CONSTRAINT chk_events_min_rating_range
        CHECK (min_rating IS NULL OR min_rating BETWEEN 0 AND 5),

    -- FK
    CONSTRAINT fk_events_creator
        FOREIGN KEY (creator_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 3) Индексы
CREATE INDEX IF NOT EXISTS idx_events_creator_user_id
    ON events (creator_user_id);

CREATE INDEX IF NOT EXISTS idx_events_status
    ON events (status);

CREATE INDEX IF NOT EXISTS idx_events_visibility
    ON events (visibility);

CREATE INDEX IF NOT EXISTS idx_events_tags
    ON events USING GIN (tags);

-- 4) Триггер updated_at
DROP TRIGGER IF EXISTS tr_events_updated_at ON events;
CREATE TRIGGER tr_events_updated_at
BEFORE UPDATE ON events
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();