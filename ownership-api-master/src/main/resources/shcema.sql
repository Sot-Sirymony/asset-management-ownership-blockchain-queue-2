CREATE DATABASE asset_holder_db;

-- Create Role Table
CREATE TABLE Role (
                      role_id SERIAL PRIMARY KEY,
                      role_name VARCHAR(50) NOT NULL
);

-- Create Department Table
CREATE TABLE Department (
                            dep_id SERIAL PRIMARY KEY,
                            dep_name VARCHAR(100) NOT NULL,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create User Table
CREATE TABLE Users (
                       user_id SERIAL PRIMARY KEY,
                       dep_id INT REFERENCES Department(dep_id) ON DELETE SET NULL,
                       role_id INT REFERENCES Role(role_id) ON DELETE SET NULL,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       certificate TEXT,
                       dob DATE,
                       gender CHAR(6),
                       email VARCHAR(100),
                       phone_number VARCHAR(15),
                       profile_img TEXT,
                       address TEXT,
                       place_of_birth VARCHAR(255),
                       full_name VARCHAR(100),
                       role VARCHAR(50),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        description TEXT
);

-- Create Asset Request Table
CREATE TABLE Asset_Request (
                               request_id SERIAL PRIMARY KEY,
                               user_id INT REFERENCES Users(user_id) ON DELETE SET NULL,
                               asset_name VARCHAR(255) NOT NULL,
                               reason TEXT,
                               qty INT,
                               unit VARCHAR(50),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               attachment TEXT
);

