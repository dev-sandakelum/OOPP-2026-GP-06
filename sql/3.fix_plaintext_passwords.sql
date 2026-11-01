-- Fix: replace bcrypt hashes with plain-text password for development.
-- Run this ONCE against your local afms_db.
-- DO NOT run this in any production environment.

USE afms_db;

UPDATE users SET password = 'Password@123';
