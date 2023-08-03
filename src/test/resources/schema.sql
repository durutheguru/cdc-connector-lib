
USE `employee`;

CREATE TABLE user (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    address VARCHAR(100),
    gender ENUM('M', 'F')
);

