-- 1) Таблица notifications
CREATE TABLE IF NOT EXISTS notifications (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_user_id UUID NOT NULL,
    entity_type       VARCHAR(50) NOT NULL,   -- например: event, friend, comment, review, post
    type              VARCHAR(80) NOT NULL,   -- например: invite, request, accepted, new_comment, etc
    entity_id         UUID,
    is_read           BOOLEAN NOT NULL DEFAULT FALSE,

    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- защита от пустых значений
    CONSTRAINT chk_notifications_entity_type_not_blank
        CHECK (LENGTH(TRIM(entity_type)) > 0),

    CONSTRAINT chk_notifications_type_not_blank
        CHECK (LENGTH(TRIM(type)) > 0),

    -- FK
    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- 2) Индексы под частые запросы
-- "непрочитанные/последние уведомления пользователя"
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_read_created
    ON notifications (recipient_user_id, is_read, created_at DESC);

-- быстрый поиск уведомлений по сущности (например, все по event_id)
CREATE INDEX IF NOT EXISTS idx_notifications_entity
    ON notifications (entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_notifications_created_at
    ON notifications (created_at);

-- 3) Триггер updated_at
DROP TRIGGER IF EXISTS tr_notifications_updated_at ON notifications;
CREATE TRIGGER tr_notifications_updated_at
BEFORE UPDATE ON notifications
FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();