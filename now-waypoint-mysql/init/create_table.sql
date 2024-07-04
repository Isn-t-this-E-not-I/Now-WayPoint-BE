ALTER DATABASE nwpdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
/* member table 생성 */
CREATE TABLE users (
                       user_id INT(11) AUTO_INCREMENT PRIMARY KEY,
                       login_id VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       name VARCHAR(10),
                       nickname VARCHAR(30),
                       profile_image_url VARCHAR(255),
                       description VARCHAR(255),
                       locate VARCHAR(255),
                       following VARCHAR(50),
                       follower VARCHAR(50),
                       role VARCHAR(255),
                       create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
