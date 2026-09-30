CREATE DATABASE IF NOT EXISTS function_hall_db;
USE function_hall_db;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS halls;

CREATE TABLE halls (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    hall_id INT NOT NULL,
    event_date DATE NOT NULL,
    guests INT NOT NULL,
    decoration VARCHAR(60) NOT NULL,
    food_required BOOLEAN NOT NULL DEFAULT FALSE,
    food_items TEXT,
    special_request TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_hall
        FOREIGN KEY (hall_id) REFERENCES halls(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_booking_hall_date (hall_id, event_date)
);

CREATE TABLE payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_payment_booking (booking_id)
);

INSERT INTO halls.json (name, location, capacity, price_per_day) VALUES
('Royal Grand Hall', 'Main Road, City Center', 800, 35000.00),
('Green Garden Function Hall', 'Garden Road, Downtown', 500, 25000.00),
('Crystal Banquet Hall', 'Lake View Road', 300, 18000.00);
