-- Migration to create subject_category table
-- V3__create_subject_category.sql

CREATE TABLE subject_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_title VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create index for efficient querying
CREATE INDEX idx_subject_category_title ON subject_category(category_title);

