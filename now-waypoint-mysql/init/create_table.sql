ALTER DATABASE nwpdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
/* user table 생성 */
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

/* user_follower table 생성 */
CREATE TABLE user_follower(
                      follower_id INT(11) AUTO_INCREMENT PRIMARY KEY,
                      user_id INT(11) NOT NULL,
                      nickname VARCHAR(30) NOT NULL,
                      FOREIGN KEY (user_id) REFERENCES users(user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

/* user_following table 생성 */
CREATE TABLE user_following(
                       following_id INT(11) AUTO_INCREMENT PRIMARY KEY,
                       user_id INT(11) NOT NULL,
                       nickname VARCHAR(30) NOT NULL,
                       FOREIGN KEY (user_id) REFERENCES users(user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- post 테이블 생성
CREATE TABLE post (
                      post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      content TEXT NOT NULL,
                      location_tag VARCHAR(255),
                      category VARCHAR(50),
                      media_url TEXT,
                      user_id INT(11),
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      like_count INT DEFAULT 0,
                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- post_likes 테이블 생성
CREATE TABLE post_likes (
                            like_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            post_id BIGINT NOT NULL,
                            user_id INT(11) NOT NULL,
                            FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- hashtag 테이블 생성
CREATE TABLE hashtag (
                         hashtag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) UNIQUE NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- post_hashtags 테이블 생성 (다대다 관계를 위한 조인 테이블)
CREATE TABLE post_hashtags (
                               post_id BIGINT NOT NULL,
                               hashtag_id BIGINT NOT NULL,
                               PRIMARY KEY (post_id, hashtag_id),
                               FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
                               FOREIGN KEY (hashtag_id) REFERENCES hashtag(hashtag_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- notify 테이블 생성
CREATE TABLE notify (
                        notify_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        sender_nickname VARCHAR(30) NOT NULL,
                        profile_image_url VARCHAR(255),
                        message TEXT NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;