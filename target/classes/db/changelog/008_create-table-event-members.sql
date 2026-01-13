-- 1) ENUM'ы для event_members
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_member_status') THEN
        CREATE TYPE event_member_status AS ENUM (
            'invite_pending',
            'request_pending',
            'member',
            'rejected',
            'deleted'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_member_role') THEN
        CREATE TYPE event_member_role AS ENUM (
            'common',
            'organizer',
            'can_invite'
        );
    END IF;
END $$;

-- 2) Таблица event_members
CREATE TABLE IF NOT EXISTS event_members (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id        UUID NOT NULL,
    member_user_id  UUID NOT NULL,
    status          event_member_status NOT NULL,
    role            event_member_role NOT NULL DEFAULT 'common',

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- FK
    CONSTRAINT fk_event_members_event
        FOREIGN KEY (event_id)
        REFERENCES events (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_event_members_member
        FOREIGN KEY (member_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 3) Один пользователь = одна запись на ивент
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_members_event_user
    ON event_members (event_id, member_user_id);

-- 4) Индексы под частые запросы
CREATE INDEX IF NOT EXISTS idx_event_members_event_id
    ON event_members (event_id);

CREATE INDEX IF NOT EXISTS idx_event_members_member_user_id
    ON event_members (member_user_id);

CREATE INDEX IF NOT EXISTS idx_event_members_status
    ON event_members (status);

CREATE INDEX IF NOT EXISTS idx_event_members_role
    ON event_members (role);

-- 5) Триггер updated_at
DROP TRIGGER IF EXISTS tr_event_members_updated_at ON event_members;
CREATE TRIGGER tr_event_members_updated_at
BEFORE UPDATE ON event_members
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();