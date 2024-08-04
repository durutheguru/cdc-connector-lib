
USE `employee`;

CREATE TABLE IF NOT EXISTS user (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    address VARCHAR(100),
    gender ENUM('M', 'F')
);

