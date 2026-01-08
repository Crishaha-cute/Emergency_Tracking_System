-- Emergency Tracking System Database Schema
-- Execute this in phpMyAdmin (Laragon)

CREATE DATABASE IF NOT EXISTS emergency_tracking_system;
USE emergency_tracking_system;

-- Users Table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'RESPONDER', 'ADMIN') NOT NULL,
    emergency_contact VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Emergencies Table
CREATE TABLE emergencies (
    emergency_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    responder_id INT,
    emergency_type ENUM('MEDICAL', 'FIRE', 'CRIME', 'ACCIDENT') NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'RESOLVED') DEFAULT 'PENDING',
    description TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    location_address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (responder_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Notifications Table
CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    emergency_id INT,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (emergency_id) REFERENCES emergencies(emergency_id) ON DELETE CASCADE
);

-- Insert sample users (password: "password123" - should be hashed in production)
INSERT INTO users (full_name, email, phone_number, username, password, role, emergency_contact) VALUES
('John Doe', 'john.doe@email.com', '+1234567890', 'johndoe', 'password123', 'USER', '+0987654321'),
('Jane Smith', 'jane.smith@email.com', '+1234567891', 'janesmith', 'password123', 'RESPONDER', NULL),
('Emergency Responder', 'responder@emergency.com', '+1234567892', 'responder1', 'password123', 'RESPONDER', NULL),
('Admin User', 'admin@emergency.com', '+1234567893', 'admin', 'admin123', 'ADMIN', NULL);

-- Sample emergency (optional)
INSERT INTO emergencies (user_id, emergency_type, status, description, latitude, longitude, location_address) VALUES
(1, 'MEDICAL', 'PENDING', 'Heart attack emergency', 40.7128, -74.0060, '123 Main St, New York, NY');

-- Create indexes for better performance
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_emergency_status ON emergencies(status);
CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_read ON notifications(is_read);