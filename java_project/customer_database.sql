create database shoppingcartsystem;
use shoppingcartsystem;


CREATE TABLE User (
    id INT AUTO_INCREMENT PRIMARY KEY,   -- 사용자 고유 ID (자동 증가)
    name VARCHAR(100) NOT NULL,          -- 사용자 이름
    email VARCHAR(100) NOT NULL UNIQUE,  -- 이메일 (고유값)
    password VARCHAR(255) NOT NULL,      -- 비밀번호
    role ENUM('admin', 'customer') NOT NULL, -- 사용자 역할 (admin 또는 customer)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 생성 날짜
);

INSERT INTO User (name, email, password, role) 
VALUES 
('test_customer', 'john@example.com', 'password123', 'customer'),
('Admin', 'admin@example.com', 'admin123', 'admin');
