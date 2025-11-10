SET NAMES utf8mb4;

CREATE TABLE `member_tbl` (
                              `member_id` bigint NOT NULL AUTO_INCREMENT,
                              `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                              `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
                              `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                              `role` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'USER',
                              `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'ACTIVE',
                              `created_at` datetime DEFAULT NULL,
                              `updated_at` datetime DEFAULT NULL,
                              `deleted_at` datetime DEFAULT NULL,
                              PRIMARY KEY (`member_id`),
                              UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `board_type_tbl` (
                                  `board_type_id` bigint NOT NULL AUTO_INCREMENT,
                                  `code` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
                                  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                                  `description` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                  PRIMARY KEY (`board_type_id`),
                                  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `board_tbl` (
                             `board_id` bigint NOT NULL AUTO_INCREMENT,
                             `board_type_id` bigint NOT NULL,
                             `writer_id` bigint NOT NULL,
                             `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
                             `content` text COLLATE utf8mb4_general_ci NOT NULL,
                             `view_count` int DEFAULT '0',
                             `like_count` int DEFAULT '0',
                             `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'ACTIVE',
                             `created_at` datetime DEFAULT NULL,
                             `updated_at` datetime DEFAULT NULL,
                             `deleted_at` datetime DEFAULT NULL,
                             PRIMARY KEY (`board_id`),
                             KEY `board_type_id` (`board_type_id`),
                             KEY `member_id` (`writer_id`),
                             CONSTRAINT `board_tbl_ibfk_1` FOREIGN KEY (`board_type_id`) REFERENCES `board_type_tbl` (`board_type_id`),
                             CONSTRAINT `board_tbl_ibfk_2` FOREIGN KEY (`writer_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `comment_tbl` (
                               `comment_id` bigint NOT NULL AUTO_INCREMENT,
                               `board_id` bigint NOT NULL,
                               `writer_id` bigint NOT NULL,
                               `parent_id` bigint DEFAULT NULL,
                               `content` text COLLATE utf8mb4_general_ci NOT NULL,
                               `like_count` int DEFAULT '0',
                               `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'ACTIVE',
                               `created_at` datetime DEFAULT NULL,
                               `updated_at` datetime DEFAULT NULL,
                               `deleted_at` datetime DEFAULT NULL,
                               PRIMARY KEY (`comment_id`),
                               KEY `board_id` (`board_id`),
                               KEY `member_id` (`writer_id`),
                               KEY `parent_id` (`parent_id`),
                               CONSTRAINT `comment_tbl_ibfk_1` FOREIGN KEY (`board_id`) REFERENCES `board_tbl` (`board_id`),
                               CONSTRAINT `comment_tbl_ibfk_2` FOREIGN KEY (`writer_id`) REFERENCES `member_tbl` (`member_id`),
                               CONSTRAINT `comment_tbl_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `comment_tbl` (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `like_tbl` (
                            `like_id` bigint NOT NULL AUTO_INCREMENT,
                            `member_id` bigint NOT NULL,
                            `related_type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                            `related_id` bigint NOT NULL,
                            `created_at` datetime DEFAULT NULL,
                            PRIMARY KEY (`like_id`),
                            UNIQUE KEY `member_id` (`member_id`,`related_type`,`related_id`),
                            CONSTRAINT `like_tbl_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `file_tbl` (
                            `file_id` bigint NOT NULL AUTO_INCREMENT,
                            `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                            `related_id` bigint NOT NULL,
                            `original_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
                            `unique_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
                            `file_order` int DEFAULT '0',
                            `file_path` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
                            `file_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
                            `file_size` bigint DEFAULT NULL,
                            `created_at` datetime DEFAULT NULL,
                            `updated_at` datetime DEFAULT NULL,
                            `deleted_at` datetime DEFAULT NULL,
                            PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `board_type_tbl` (`board_type_id`, `code`, `name`, `description`) VALUES
                                                                                  (1, 'free', '자유게시판', '일반적인 자유 게시판'),
                                                                                  (2, 'review', '리뷰게시판', '서비스/상품 리뷰 게시판'),
                                                                                  (3, 'event', '이벤트게시판', '이벤트 안내 및 참여 게시판'),
                                                                                  (4, 'resource', '자료실', '파일 및 자료 공유 게시판'),
                                                                                  (5, 'notice', '공지사항', '공지 및 안내 게시판');