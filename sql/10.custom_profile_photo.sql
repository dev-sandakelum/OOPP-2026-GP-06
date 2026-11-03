-- =====================================================
-- Allow user-uploaded profile pictures
-- =====================================================
-- Uploaded photos are saved by the app in uploads/photos/ with names like
-- u12_1730500000000.png, so the photo column must hold longer filenames than
-- the bundled b1.jpg / g5.jpg ones. NULL / bundled values keep working.
ALTER TABLE users
    MODIFY COLUMN photo VARCHAR(255) DEFAULT NULL
    COMMENT 'Profile photo filename: bundled (b1.jpg, g5.jpg) or uploaded (uploads/photos/u<id>_<time>.jpg|png)';
