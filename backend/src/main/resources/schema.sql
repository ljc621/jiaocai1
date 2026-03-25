CREATE TABLE IF NOT EXISTS `User` (
    id VARCHAR(64) PRIMARY KEY,
    phone VARCHAR(20),
    email VARCHAR(100),
    password VARCHAR(100),
    name VARCHAR(50),
    university VARCHAR(100),
    student_id VARCHAR(50),
    avatar VARCHAR(255),
    credit_score INT,
    balance DOUBLE,
    points INT,
    verified BOOLEAN,
    role VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `address` (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    recipient VARCHAR(50),
    phone VARCHAR(20),
    detail VARCHAR(255),
    is_default BOOLEAN
);

CREATE TABLE IF NOT EXISTS `Textbook` (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(200),
    author VARCHAR(100),
    isbn VARCHAR(20),
    publisher VARCHAR(100),
    price DOUBLE,
    original_price DOUBLE,
    daily_rent DOUBLE,
    rent_enabled BOOLEAN,
    description TEXT,
    `condition` VARCHAR(50),
    category VARCHAR(50),
    cover VARCHAR(255),
    images TEXT,
    seller_id VARCHAR(64),
    status VARCHAR(20),
    type VARCHAR(20) DEFAULT 'sell',
    views INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `Order` (
    id VARCHAR(64) PRIMARY KEY,
    type VARCHAR(20),
    textbook_id VARCHAR(64),
    buyer_id VARCHAR(64),
    seller_id VARCHAR(64),
    amount DOUBLE,
    status VARCHAR(20),
    shipping_status VARCHAR(20),
    tracking_number VARCHAR(100),
    address_id VARCHAR(64),
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    completed_at TIMESTAMP,
    review_content TEXT,
    review_rating INT,
    rent_duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `posts` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64),
    title VARCHAR(200),
    content TEXT,
    type VARCHAR(20),
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `comments` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT,
    user_id VARCHAR(64),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `message` (
    id VARCHAR(64) PRIMARY KEY,
    sender_id VARCHAR(64),
    receiver_id VARCHAR(64),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `WalletTransaction` (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    amount DOUBLE,
    type VARCHAR(20),
    status VARCHAR(20),
    description VARCHAR(255),
    balance DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `review` (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    seller_id VARCHAR(64),
    textbook_id VARCHAR(64),
    rating INT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `PointTransaction` (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    points INT,
    type VARCHAR(50),
    description VARCHAR(255),
    balance INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `verification_request` (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    real_name VARCHAR(50),
    student_id VARCHAR(50),
    material_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
