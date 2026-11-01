-- =====================================================
-- Add Photo Feature to Users Table
-- =====================================================
-- This script:
-- 1. Adds the photo column to the users table
-- 2. Creates a trigger to auto-assign photos to new users
-- 3. Assigns unique photos to existing users based on gender
--
-- Boy photos: b1.jpg to b15.jpg (15 photos)
-- Girl photos: g1.jpg to g10.jpg (10 photos)
-- =====================================================

-- Step 1: Add photo column to users table
ALTER TABLE users ADD COLUMN photo VARCHAR(50) DEFAULT NULL 
    COMMENT 'Profile photo filename (e.g., b1.jpg, g5.jpg)';

-- Step 2: Create trigger to auto-assign photos to new users
DELIMITER $$

CREATE TRIGGER assign_photo_before_insert
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    DECLARE photo_number INT;
    DECLARE photo_prefix CHAR(1);
    DECLARE max_photos INT;
    
    -- Only assign photo if it's NULL
    IF NEW.photo IS NULL THEN
        -- Determine photo prefix and max count based on gender
        IF NEW.gender = 'FEMALE' THEN
            SET photo_prefix = 'g';
            SET max_photos = 10;
        ELSE
            SET photo_prefix = 'b';
            SET max_photos = 15;
        END IF;
        
        -- Find the first available photo number
        SET photo_number = 1;
        WHILE photo_number <= max_photos DO
            -- Check if this photo is already in use
            IF NOT EXISTS (
                SELECT 1 FROM users 
                WHERE photo = CONCAT(photo_prefix, photo_number, '.jpg')
            ) THEN
                -- Assign this photo
                SET NEW.photo = CONCAT(photo_prefix, photo_number, '.jpg');
                SET photo_number = max_photos + 1; -- Exit loop
            ELSE
                SET photo_number = photo_number + 1;
            END IF;
        END WHILE;
        
        -- If all photos are taken, cycle back to the first one
        IF NEW.photo IS NULL THEN
            SET NEW.photo = CONCAT(photo_prefix, '1.jpg');
        END IF;
    END IF;
END$$

DELIMITER ;

-- Step 3: Assign photos to existing FEMALE users (g1.jpg to g10.jpg)
SET @female_counter = 0;
UPDATE users 
SET photo = CONCAT('g', (@female_counter := @female_counter + 1), '.jpg')
WHERE gender = 'FEMALE'
  AND photo IS NULL
  AND @female_counter < 11
ORDER BY user_id;

-- Step 4: Assign photos to existing MALE users (b1.jpg to b15.jpg)
SET @male_counter = 0;
UPDATE users 
SET photo = CONCAT('b', (@male_counter := @male_counter + 1), '.jpg')
WHERE gender = 'MALE'
  AND photo IS NULL
  AND @male_counter < 16
ORDER BY user_id;

-- Step 5: Assign photos to OTHER or NULL gender users (use boy photos)
SET @other_counter = 0;
UPDATE users 
SET photo = CONCAT('b', ((@other_counter := @other_counter + 1) % 15) + 1, '.jpg')
WHERE (gender = 'OTHER' OR gender IS NULL)
  AND photo IS NULL
ORDER BY user_id;

-- =====================================================
-- Verification Queries (Optional - uncomment to run)
-- =====================================================

-- View all users with their assigned photos
-- SELECT user_id, name, gender, photo FROM users ORDER BY gender, user_id;

-- Count users by gender and check photo assignments
-- SELECT 
--     gender,
--     COUNT(*) as total_users,
--     COUNT(photo) as users_with_photo,
--     COUNT(*) - COUNT(photo) as users_without_photo
-- FROM users
-- GROUP BY gender;

-- Check for duplicate photo assignments (should return 0 rows ideally)
-- SELECT photo, COUNT(*) as count
-- FROM users
-- WHERE photo IS NOT NULL
-- GROUP BY photo
-- HAVING COUNT(*) > 1;

-- List all unique photos in use
-- SELECT DISTINCT photo FROM users WHERE photo IS NOT NULL ORDER BY photo;

-- Test the trigger (uncomment to test)
-- INSERT INTO users (name, email, password, user_type, gender) 
-- VALUES ('Test User', 'test@example.com', 'password123', 'UNDERGRADUATE', 'FEMALE');
-- SELECT user_id, name, gender, photo FROM users WHERE email = 'test@example.com';
