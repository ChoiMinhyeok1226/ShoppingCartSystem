-- 기존 데이터베이스가 존재하면 삭제
DROP DATABASE IF EXISTS shoppingcartsystem;

-- 데이터베이스 생성
CREATE DATABASE shoppingcartsystem;
USE shoppingcartsystem;

-- User 테이블이 존재하면 삭제 후 생성
DROP TABLE IF EXISTS User;

CREATE TABLE User (
    id INT AUTO_INCREMENT PRIMARY KEY,   -- 사용자 고유 ID (자동 증가)
    name VARCHAR(100) NOT NULL,          -- 사용자 이름
    email VARCHAR(100) NOT NULL UNIQUE,  -- 이메일 (고유값)
    password VARCHAR(255) NOT NULL,      -- 비밀번호
    role ENUM('admin', 'customer') NOT NULL, -- 사용자 역할 (admin 또는 customer)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 생성 날짜
);

-- User 테이블 기본 데이터 삽입
INSERT INTO User (name, email, password, role) 
VALUES 
    ('test_customer', 'john@example.com', 'password123', 'customer'),
    ('Admin', 'admin@example.com', 'admin123', 'admin');

-- Inventory 테이블이 존재하면 삭제 후 생성
DROP TABLE IF EXISTS Inventory;

CREATE TABLE Inventory (
    product_id INT AUTO_INCREMENT PRIMARY KEY, -- 제품 ID (자동 증가)
    product_name VARCHAR(100) NOT NULL,        -- 제품 이름
    category VARCHAR(50),                      -- 제품 카테고리
    description TEXT,                          -- 제품 설명
    quantity INT NOT NULL,                     -- 재고 수량
    price DECIMAL(10, 2) NOT NULL,             -- 가격
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 추가 날짜
);

-- Inventory 테이블 기본 데이터 삽입
INSERT INTO Inventory (product_name, category, description, quantity, price)
VALUES 
    ('Laptop', 'Electronics', 'High-performance laptop', 10, 1500.00),
    ('Smartphone', 'Electronics', 'Latest model smartphone', 20, 800.00),
    ('Desk Chair', 'Furniture', 'Ergonomic desk chair', 15, 120.00),
    ('Notebook', 'Stationery', 'A5 size ruled notebook', 100, 2.50),
    ('Pen', 'Stationery', 'Black ballpoint pen', 200, 0.50);
    
    CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_price DECIMAL(10, 2),
    status VARCHAR(20),
    FOREIGN KEY (customer_id) REFERENCES User(id)
);

CREATE TABLE OrderDetails (
    order_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    product_name VARCHAR(100) NOT NULL,
    product_id INT,
    quantity INT,
    price DECIMAL(10, 2),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Inventory(product_id)
);
