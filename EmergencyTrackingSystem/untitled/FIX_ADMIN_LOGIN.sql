-- ============================================
-- FIX ADMIN LOGIN - Run these SQL commands
-- ============================================
-- Execute these in phpMyAdmin or MySQL command line

USE emergency_tracking_system;

-- Step 1: Update the role ENUM to include ADMIN
ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'RESPONDER', 'ADMIN') NOT NULL;

-- Step 2: Check if admin user exists
SELECT * FROM users WHERE username = 'admin';

-- Step 3: If no admin user exists, create one
-- Option A: Use default admin credentials
INSERT INTO users (full_name, email, phone_number, username, password, role, emergency_contact) 
VALUES ('Admin User', 'admin@emergency.com', '+1234567893', 'admin', 'admin123', 'ADMIN', NULL)
ON DUPLICATE KEY UPDATE role = 'ADMIN';

-- Step 4: Verify the admin user was created
SELECT user_id, full_name, username, role FROM users WHERE role = 'ADMIN';

-- Step 5: If you want to update an existing user to admin:
-- UPDATE users SET role = 'ADMIN' WHERE username = 'your_username_here';

-- ============================================
-- TROUBLESHOOTING QUERIES
-- ============================================

-- Check all users and their roles:
SELECT user_id, full_name, username, role FROM users;

-- Check if ADMIN is in the ENUM:
SHOW COLUMNS FROM users WHERE Field = 'role';

-- Verify a specific user exists with correct role:
SELECT * FROM users WHERE username = 'admin' AND password = 'admin123' AND role = 'ADMIN';

