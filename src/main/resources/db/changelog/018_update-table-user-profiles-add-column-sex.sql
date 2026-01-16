-- 1) ENUM для пола
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_sex') THEN
        CREATE TYPE user_sex AS ENUM ('male', 'female');
    END IF;
END $$;

-- 2) Добавляем колонку sex
ALTER TABLE user_profiles
ADD COLUMN IF NOT EXISTS sex user_sex;