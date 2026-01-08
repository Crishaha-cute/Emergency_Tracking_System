-- ============================================
-- URGENT: Fix Admin Role Error
-- ============================================
-- This error means the database ENUM doesn't include ADMIN
-- Run this FIRST before trying to register/login as admin

USE emergency_tracking_system;

-- This is the CRITICAL command that fixes the error:
ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'RESPONDER', 'ADMIN') NOT NULL;

-- Verify it worked:
SHOW COLUMNS FROM users WHERE Field = 'role';
-- You should see: enum('USER','RESPONDER','ADMIN')

-- Now create admin user:
INSERT INTO users (full_name, email, phone_number, username, password, role, emergency_contact) 
VALUES ('Admin User', 'admin@emergency.com', '+1234567893', 'admin', 'admin123', 'ADMIN', NULL);

-- Verify admin was created:
SELECT * FROM users WHERE role = 'ADMIN';

