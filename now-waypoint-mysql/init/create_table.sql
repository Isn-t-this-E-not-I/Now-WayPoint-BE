-- 데이터베이스의 기본 문자 집합과 대조를 설정
ALTER DATABASE nwpdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- users 테이블 생성
CREATE TABLE users (
                       user_id INT(11) AUTO_INCREMENT PRIMARY KEY,
                       login_id VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       name VARCHAR(10),
                       nickname VARCHAR(30),
                       profile_image_url VARCHAR(255),
                       description VARCHAR(255),
                       locate VARCHAR(255),
                       email VARCHAR(50),
                       following VARCHAR(50),
                       follower VARCHAR(50),
                       role VARCHAR(255),
                       create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       active VARCHAR(10) DEFAULT 'true' -- 추가된 active 컬럼
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- user_follower 테이블 생성
CREATE TABLE user_follower(
                              follower_id INT(11) AUTO_INCREMENT PRIMARY KEY,
                              user_id INT(11) NOT NULL,
                              nickname VARCHAR(30) NOT NULL,
                              FOREIGN KEY (user_id) REFERENCES users(user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- user_following 테이블 생성
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
                      user_id INT(11),
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      like_count INT DEFAULT 0,
                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- post_media_urls 테이블 생성
CREATE TABLE post_media_urls (
                                 post_id BIGINT NOT NULL,
                                 media_url TEXT NOT NULL,
                                 FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE
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
                               order_index INT, -- 추가된 order_index 컬럼
                               PRIMARY KEY (post_id, hashtag_id),
                               FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
                               FOREIGN KEY (hashtag_id) REFERENCES hashtag(hashtag_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- notify 테이블 생성
CREATE TABLE notify (
                        notify_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        sender_nickname VARCHAR(30) NOT NULL,
                        receiver_nickname VARCHAR(30) NOT NULL,
                        profile_image_url VARCHAR(255),
                        message TEXT NOT NULL,
                        create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        comment VARCHAR(255), -- 추가된 comment 컬럼
                        media_url VARCHAR(255), -- 추가된 media_url 컬럼
                        post_id BIGINT -- 추가된 post_id 컬럼
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- chat_room 테이블 생성
CREATE TABLE chat_room (
                           chat_room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- user_chat_room 테이블 생성
CREATE TABLE user_chat_room (
                                user_chat_room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id INT(11) NOT NULL,
                                chat_room_id BIGINT NOT NULL,
                                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (chat_room_id) REFERENCES chat_room(chat_room_id) ON DELETE CASCADE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- comment 테이블 생성
CREATE TABLE comment (
                         comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         content TEXT NOT NULL,
                         post_id BIGINT NOT NULL,
                         user_id INT(11) NOT NULL,
                         parent_id BIGINT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (parent_id) REFERENCES comment(comment_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- comment_like 테이블 생성
CREATE TABLE comment_like (
                              comment_like_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              comment_id BIGINT NOT NULL,
                              user_id INT(11) NOT NULL,
                              FOREIGN KEY (comment_id) REFERENCES comment(comment_id) ON DELETE CASCADE,
                              FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
