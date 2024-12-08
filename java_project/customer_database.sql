-- 기존 데이터베이스가 존재하면 삭제
DROP DATABASE IF EXISTS shoppingcartsystem;

-- 데이터베이스 생성
CREATE DATABASE shoppingcartsystem;
USE shoppingcartsystem;

-- User 테이블 생성
CREATE TABLE User (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(100) NOT NULL,
                      email VARCHAR(100) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      role ENUM('admin', 'customer') NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 초기 데이터 삽입
INSERT INTO User (name, email, password, role)
VALUES
    ('test_customer', 'john@example.com', SHA2('password123', 256), 'customer'),
    ('Admin', 'admin@example.com', SHA2('admin123', 256), 'admin');

-- Inventory 테이블 생성
CREATE TABLE Inventory (
                           product_id INT AUTO_INCREMENT PRIMARY KEY,
                           product_name VARCHAR(100) NOT NULL,
                           category VARCHAR(50),
                           description TEXT,
                           quantity INT NOT NULL,
                           price DECIMAL(10, 2) NOT NULL,
                           added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 초기 데이터 삽입
INSERT INTO Inventory (product_name, category, description, quantity, price)
VALUES
    ('Laptop', 'Electronics', 'High-performance laptop', 10, 1500.00),
    ('Smartphone', 'Electronics', 'Latest model smartphone', 20, 800.00),
    ('Desk Chair', 'Furniture', 'Ergonomic desk chair', 15, 120.00),
    ('Notebook', 'Stationery', 'A5 size ruled notebook', 100, 2.50),
    ('Pen', 'Stationery', 'Black ballpoint pen', 200, 0.50);

-- Orders 테이블 생성
CREATE TABLE Orders (
                        order_id INT AUTO_INCREMENT PRIMARY KEY,
                        customer_id INT,
                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        total_price DECIMAL(10, 2),
                        status ENUM('Pending', 'Completed', 'Cancelled') DEFAULT 'Pending',
                        FOREIGN KEY (customer_id) REFERENCES User(id)
);

-- OrderDetails 테이블 생성
CREATE TABLE OrderDetails (
                              order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
                              order_id INT,
                              product_name VARCHAR(100) NOT NULL,
                              product_id INT,
                              quantity INT,
                              price DECIMAL(10, 2),
                              FOREIGN KEY (order_id) REFERENCES Orders(order_id),
                              FOREIGN KEY (product_id) REFERENCES Inventory(product_id)
);

-- Board 테이블 생성
CREATE TABLE Board (
                       post_id INT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       content TEXT NOT NULL,
                       author VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 초기 데이터 삽입
INSERT INTO Board (title, content, author)
VALUES
    ('Welcome to the Board!', 'This is the first post on our board.', 'Admin'),
    ('How to Use the Shopping Cart', 'Here is a guide on how to use the cart system.', 'Admin');
