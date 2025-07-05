-- ==================== 电影购票系统数据库脚本 ====================
-- 创建数据库
CREATE DATABASE IF NOT EXISTS movie_booking_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE movie_booking_system;

-- 删除已存在的表
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS movie_sessions;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS seats_sessions;
DROP TABLE IF EXISTS halls;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_groups;
DROP TABLE IF EXISTS user_group_relations;

-- ==================== 用户管理模块 ====================

-- 创建角色表
CREATE TABLE roles
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name         VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色名称',
    display_name VARCHAR(100) NOT NULL COMMENT '角色显示名称',
    description  VARCHAR(255) COMMENT '角色描述',
    created_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- 创建用户表
CREATE TABLE users
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码',
    email       VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone       VARCHAR(20) UNIQUE COMMENT '手机号',
    open_id     VARCHAR(100) UNIQUE COMMENT 'QID',
    avatar      VARCHAR(255) COMMENT '头像URL',
    role_id     BIGINT       NOT NULL COMMENT '角色ID',
    status      ENUM ('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '状态',
    last_login  TIMESTAMP    NULL COMMENT '最后登录时间',
    login_count INT                                   DEFAULT 0 COMMENT '登录次数',
    created_at  TIMESTAMP                             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)                            DEFAULT 0 COMMENT '是否删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_role_id (role_id),
    INDEX idx_status (status),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 创建用户组表
CREATE TABLE user_groups
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户组ID',
    name        VARCHAR(50) NOT NULL UNIQUE COMMENT '用户组名称',
    description VARCHAR(255) COMMENT '用户组描述',
    type        ENUM ('SYSTEM', 'CUSTOM') DEFAULT 'CUSTOM' COMMENT '用户组类型',
    created_at  TIMESTAMP                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1)                DEFAULT 0 COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户组表';

-- 创建用户组关系表
CREATE TABLE user_group_relations
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    user_id    BIGINT NOT NULL COMMENT '用户ID',
    group_id   BIGINT NOT NULL COMMENT '用户组ID',
    created_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_user_group (user_id, group_id),
    INDEX idx_user_id (user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_group_group FOREIGN KEY (group_id) REFERENCES user_groups (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户组关系表';

-- ==================== 电影管理模块 ====================

-- 创建电影表
CREATE TABLE movies
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '电影ID',
    title            VARCHAR(200) NOT NULL COMMENT '电影标题',
    description      TEXT COMMENT '电影描述',
    director         VARCHAR(100) COMMENT '导演',
    actors           TEXT COMMENT '演员',
    genre            VARCHAR(100) COMMENT '类型',
    duration_minutes INT COMMENT '时长（分钟）',
    release_date     DATE COMMENT '上映日期',
    end_date         DATE COMMENT '下映日期',
    poster_url       VARCHAR(255) COMMENT '海报URL',
    trailer_url      VARCHAR(255) COMMENT '预告片URL',
    base_price       DECIMAL(10, 2)                            DEFAULT 0.00 COMMENT '基础票价',
    status           ENUM ('UPCOMING', 'NOW_SHOWING', 'ENDED') DEFAULT 'UPCOMING' COMMENT '状态',
    rating           DECIMAL(3, 1)                             DEFAULT 0.0 COMMENT '评分',
    rating_count     INT                                       DEFAULT 0 COMMENT '评分人数',
    view_count       INT                                       DEFAULT 0 COMMENT '观看次数',
    box_office       DECIMAL(15, 2)                            DEFAULT 0.00 COMMENT '票房',
    language         VARCHAR(50) COMMENT '语言',
    country          VARCHAR(50) COMMENT '国家/地区',
    created_at       TIMESTAMP                                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)                                DEFAULT 0 COMMENT '是否删除',
    INDEX idx_title (title),
    INDEX idx_genre (genre),
    INDEX idx_status (status),
    INDEX idx_release_date (release_date),
    INDEX idx_rating (rating),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电影表';

-- ==================== 影厅管理模块 ====================

-- 创建影厅表
CREATE TABLE halls
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '影厅ID',
    hall_name        VARCHAR(100) NOT NULL COMMENT '影厅名称',
    total_seats      INT          NOT NULL COMMENT '总座位数',
    total_rows       INT          NOT NULL COMMENT '总行数',
    total_columns    INT          NOT NULL COMMENT '总列数',
    hall_type        ENUM ('REGULAR', 'VIP', 'IMAX', 'DOLBY') DEFAULT 'REGULAR' COMMENT '影厅类型',
    status           ENUM ('ACTIVE', 'MAINTENANCE')           DEFAULT 'ACTIVE' COMMENT '状态',
    price_multiplier DECIMAL(3, 2)                            DEFAULT 1.00 COMMENT '价格倍数，根据影厅类型计算',
    created_at       TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)                               DEFAULT 0 COMMENT '是否删除',
    INDEX idx_hall_type (hall_type),
    INDEX idx_status (status),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='影厅表';

-- 创建座位表
CREATE TABLE seats
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    hall_id          BIGINT      NOT NULL COMMENT '影厅ID',
    seat_number      VARCHAR(20) NOT NULL COMMENT '座位号',
    seat_row         INT         NOT NULL COMMENT '行号',
    seat_column      INT         NOT NULL COMMENT '列号',
    seat_type        ENUM ('REGULAR', 'VIP') DEFAULT 'REGULAR' COMMENT '座位类型',
    price_multiplier DECIMAL(3, 2)           DEFAULT 1.00 COMMENT '价格倍数，根据座位类型计算',
    created_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)              DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_hall_seat (hall_id, seat_number),
    INDEX idx_hall_id (hall_id),
    INDEX idx_seat_type (seat_type),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_seats_hall_id FOREIGN KEY (hall_id) REFERENCES halls (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='座位表';

-- ==================== 场次管理模块 ====================

-- 创建电影场次表
CREATE TABLE movie_sessions
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '场次ID',
    movie_id     BIGINT   NOT NULL COMMENT '电影ID',
    hall_id      BIGINT   NOT NULL COMMENT '影厅ID',
    session_time DATETIME NOT NULL COMMENT '开始时间',
    end_time     DATETIME NOT NULL COMMENT '结束时间',
    created_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_movie_id (movie_id),
    INDEX idx_hall_id (hall_id),
    INDEX idx_session_time (session_time),
    INDEX idx_movie_time (movie_id, session_time),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_movie_sessions_movie_id FOREIGN KEY (movie_id) REFERENCES movies (id),
    CONSTRAINT fk_movie_sessions_hall_id FOREIGN KEY (hall_id) REFERENCES halls (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电影场次表';

-- 创建座位场次关联表
CREATE TABLE seats_sessions
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    seat_id    BIGINT NOT NULL COMMENT '座位ID',
    session_id BIGINT NOT NULL COMMENT '场次ID',
    status     ENUM ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE' COMMENT '状态',
    created_at TIMESTAMP                                                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP                                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1)                                                DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_seat_session (seat_id, session_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_session_id (session_id),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
    CONSTRAINT fk_session FOREIGN KEY (session_id) REFERENCES movie_sessions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='座位场次关联表';

-- ==================== 订单管理模块 ====================

-- 创建订单表
CREATE TABLE orders
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_number   VARCHAR(50)    NOT NULL UNIQUE COMMENT '订单号',
    user_id        BIGINT         NOT NULL COMMENT '用户ID',
    session_id     BIGINT         NOT NULL COMMENT '场次ID',
    seat_numbers   TEXT           NOT NULL COMMENT '座位号（逗号分隔）',
    ticket_count   INT            NOT NULL COMMENT '票数',
    total_amount   DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    status         ENUM ('PENDING', 'PAID', 'CANCELLED', 'REFUNDED', 'COMPLETED') DEFAULT 'PENDING' COMMENT '订单状态',
    payment_method ENUM ('ALIPAY', 'WECHAT', 'BANK_CARD', 'CASH') COMMENT '支付方式',
    payment_time   DATETIME COMMENT '支付时间',
    created_at     TIMESTAMP                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     TIMESTAMP                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_rated       TINYINT(1)                                                     DEFAULT 0 COMMENT '是否评分',
    is_deleted     TINYINT(1)                                                     DEFAULT 0 COMMENT '是否删除',
    INDEX idx_order_number (order_number),
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_orders_session_id FOREIGN KEY (session_id) REFERENCES movie_sessions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表';

-- 创建订单项表
CREATE TABLE order_items
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id   BIGINT         NOT NULL COMMENT '订单ID',
    seat_id    BIGINT         NOT NULL COMMENT '座位ID',
    price      DECIMAL(10, 2) NOT NULL COMMENT '价格',
    created_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_order_id (order_id),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_item_seat FOREIGN KEY (seat_id) REFERENCES seats (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单项表';

-- ==================== 支付管理模块 ====================

-- 创建支付记录表
CREATE TABLE payments
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付ID',
    order_id         BIGINT                                         NOT NULL COMMENT '订单ID',
    user_id          BIGINT                                         NOT NULL COMMENT '用户ID',
    payment_method   ENUM ('ALIPAY', 'WECHAT', 'BANK_CARD', 'CASH') NOT NULL COMMENT '支付方式',
    payment_amount   DECIMAL(10, 2)                                 NOT NULL COMMENT '支付金额',
    transaction_id   VARCHAR(100) COMMENT '交易ID',
    payment_status   ENUM ('PENDING', 'PAID', 'COMPLETED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING' COMMENT '支付状态',
    payment_time     DATETIME COMMENT '支付时间',
    refund_amount    DECIMAL(10, 2) COMMENT '退款金额',
    refund_time      DATETIME COMMENT '退款时间',
    refund_reason    VARCHAR(255) COMMENT '退款原因',
    gateway_response TEXT COMMENT '网关响应',
    gateway_code     VARCHAR(50) COMMENT '网关代码',
    gateway_message  VARCHAR(255) COMMENT '网关消息',
    created_at       TIMESTAMP                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)                                                     DEFAULT 0 COMMENT '是否删除',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_time (payment_time),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='支付记录表';

-- ==================== 系统管理模块 ====================
-- 创建事件调度器，每天检查并更新电影状态
SET GLOBAL event_scheduler = ON;

-- 删除已存在的事件（如果存在）
DROP EVENT IF EXISTS update_movie_status_event;
DROP EVENT IF EXISTS update_session_orders_event;
DROP EVENT IF EXISTS process_ended_sessions_event;

-- 创建每天执行一次的事件，用于更新电影状态
-- ALTER EVENT update_movie_status_event
-- ON SCHEDULE EVERY 1 DAY
-- STARTS TIMESTAMP(CURRENT_DATE, '18:00:00');
CREATE EVENT update_movie_status_event
    ON SCHEDULE EVERY 1 DAY
        STARTS TIMESTAMP(CURRENT_DATE, '00:00:00')
    DO
    BEGIN
        -- 将状态从 UPCOMING 更新为 NOW_SHOWING（当前日期 >= 上映日期）
        UPDATE movies
        SET status     = 'NOW_SHOWING',
            updated_at = NOW()
        WHERE status = 'UPCOMING'
          AND release_date IS NOT NULL
          AND release_date <= CURDATE()
          AND is_deleted = 0;

        -- 将状态从 NOW_SHOWING 更新为 ENDED（当前日期 >= 下映日期）
        UPDATE movies
        SET status     = 'ENDED',
            updated_at = NOW()
        WHERE status = 'NOW_SHOWING'
          AND end_date IS NOT NULL
          AND end_date <= CURDATE()
          AND is_deleted = 0;
    END;

-- 创建场次开始时订单状态更新事件（每5分钟执行一次）
-- ALTER EVENT update_session_orders_event
-- ON SCHEDULE EVERY 5 MINUTE;
CREATE EVENT update_session_orders_event
    ON SCHEDULE EVERY 2 MINUTE
    DO
    BEGIN
        -- 将状态从 PAID 更新为 COMPLETED（当前时间 >= 场次开始时间）
        UPDATE orders o
            JOIN movie_sessions ms ON o.session_id = ms.id
        SET o.status     = 'COMPLETED',
            o.updated_at = NOW()
        WHERE o.status = 'PAID'
          AND ms.session_time <= NOW()
          AND o.is_deleted = 0
          AND ms.is_deleted = 0;
    END;

-- 创建场次结束时数据处理事件（每5分钟执行一次）
-- ALTER EVENT process_ended_sessions_event
-- ON SCHEDULE EVERY 5 MINUTE;
CREATE EVENT process_ended_sessions_event
    ON SCHEDULE EVERY 2 MINUTE
    DO
    BEGIN
        -- 临时表存储需要处理的已结束场次
        CREATE TEMPORARY TABLE IF NOT EXISTS tmp_ended_sessions
        (
            session_id BIGINT PRIMARY KEY,
            movie_id   BIGINT
        );

        -- 查找所有已结束但未被删除的场次
        INSERT INTO tmp_ended_sessions (session_id, movie_id)
        SELECT ms.id, ms.movie_id
        FROM movie_sessions ms
        WHERE ms.end_time <= NOW()
          AND ms.is_deleted = 0;

        -- 更新电影票房和观看人数
        UPDATE movies m
            JOIN (SELECT tes.movie_id,
                         SUM(o.total_amount) AS box_office_increase,
                         SUM(o.ticket_count) AS view_count_increase
                  FROM tmp_ended_sessions tes
                           JOIN orders o ON tes.session_id = o.session_id
                  WHERE o.status = 'COMPLETED'
                    AND o.is_deleted = 0
                  GROUP BY tes.movie_id) AS stats ON m.id = stats.movie_id
        SET m.box_office = m.box_office + stats.box_office_increase,
            m.view_count = m.view_count + stats.view_count_increase,
            m.updated_at = NOW()
        WHERE m.is_deleted = 0;

        -- 逻辑删除座位场次关联记录
        UPDATE seats_sessions ss
            JOIN tmp_ended_sessions tes ON ss.session_id = tes.session_id
        SET ss.is_deleted = 1,
            ss.updated_at = NOW()
        WHERE ss.is_deleted = 0;

        -- 逻辑删除场次记录
        UPDATE movie_sessions ms
            JOIN tmp_ended_sessions tes ON ms.id = tes.session_id
        SET ms.is_deleted = 1,
            ms.updated_at = NOW()
        WHERE ms.is_deleted = 0;

        -- 删除临时表
        DROP TEMPORARY TABLE IF EXISTS tmp_ended_sessions;
    END;

-- ==================== 创建必要的索引 ====================

-- 用户相关索引
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_phone ON users (phone);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_created_at ON users (created_at);
CREATE INDEX idx_users_last_login ON users (last_login);

-- 电影相关索引
CREATE INDEX idx_movies_status ON movies (status);
CREATE INDEX idx_movies_release_date ON movies (release_date);
CREATE INDEX idx_movies_genre ON movies (genre);
CREATE INDEX idx_movies_rating ON movies (rating);
CREATE FULLTEXT INDEX ft_movies_title_desc ON movies (title, description);

-- 场次相关索引
CREATE INDEX idx_movie_sessions_hall_time ON movie_sessions (hall_id, session_time);
CREATE INDEX idx_movie_sessions_date ON movie_sessions (session_time);

-- 座位相关索引
CREATE INDEX idx_seats_hall_type ON seats (hall_id, seat_type);
CREATE INDEX idx_seats_row_column ON seats (seat_row, seat_column);

-- 座位场次关联索引
CREATE INDEX idx_seats_sessions_status ON seats_sessions (status);
CREATE INDEX idx_seats_sessions_session ON seats_sessions (session_id);

-- 订单相关索引
CREATE INDEX idx_orders_create_time ON orders (created_at);
CREATE INDEX idx_orders_user_session ON orders (user_id, session_id);
CREATE INDEX idx_orders_status_time ON orders (status, created_at);

-- 用户组关联索引
CREATE INDEX idx_user_group_relations_user ON user_group_relations (user_id);
CREATE INDEX idx_user_group_relations_group ON user_group_relations (group_id);

-- ==================== 初始化基础数据 ====================

-- 初始化角色数据（如果不存在）
INSERT IGNORE INTO roles (id, name, display_name, description)
VALUES (1, 'SUPER_ADMIN', '系统管理员', '拥有系统所有权限，包括角色管理'),
       (2, 'ADMIN', '管理员', '拥有用户、订单、电影等管理权限'),
       (3, 'USER', '普通用户', '拥有基本的浏览和购票权限');

-- 初始化用户数据
INSERT INTO users (username, password, email, phone, avatar, role_id, status, last_login, login_count, created_at)
VALUES
-- 管理员用户
('superadmin', '123456', 'superadmin@moviebooking.com', '13800000000', '/avatars/admin.jpg', 1, 'ACTIVE', NOW(), 10,
 NOW()),
('admin', '123456', 'admin@moviebooking.com', '13800000001', '/avatars/admin.jpg', 2, 'ACTIVE', NOW(), 8, NOW()),
-- 工作人员
('staff1', '123456', 'staff1@moviebooking.com', '13800000002', '/avatars/staff1.jpg', 2, 'ACTIVE', NOW(), 5, NOW()),
('staff2', '123456', 'staff2@moviebooking.com', '13800000003', '/avatars/staff2.jpg', 2, 'ACTIVE', NOW(), 4, NOW()),
-- VIP用户
('vipuser1', '123456', 'vip1@example.com', '13900000001', '/avatars/vip1.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 2 DAY), 15, DATE_SUB(NOW(), INTERVAL 120 DAY)),
('vipuser2', '123456', 'vip2@example.com', '13900000002', '/avatars/vip2.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 1 DAY), 12, DATE_SUB(NOW(), INTERVAL 90 DAY)),
-- 普通用户
('user1', '123456', 'user1@example.com', '13700000001', '/avatars/user1.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 5 DAY), 8, DATE_SUB(NOW(), INTERVAL 60 DAY)),
('user2', '123456', 'user2@example.com', '13700000002', '/avatars/user2.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 3 DAY), 6, DATE_SUB(NOW(), INTERVAL 45 DAY)),
('user3', '123456', 'user3@example.com', '13700000003', '/avatars/user3.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 1 DAY), 10, DATE_SUB(NOW(), INTERVAL 30 DAY)),
('user4', '123456', 'user4@example.com', '13700000004', '/avatars/user4.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 2 DAY), 5, DATE_SUB(NOW(), INTERVAL 20 DAY)),
('user5', '123456', 'user5@example.com', '13700000005', '/avatars/user5.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 1 DAY), 7, DATE_SUB(NOW(), INTERVAL 15 DAY)),
-- 新注册用户
('newuser1', '123456', 'new1@example.com', '13600000001', '/avatars/new1.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 1 DAY), 2, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('newuser2', '123456', 'new2@example.com', '13600000002', '/avatars/new2.jpg', 3, 'ACTIVE',
 DATE_SUB(NOW(), INTERVAL 1 DAY), 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- 休眠用户
('inactive1', '123456', 'inactive1@example.com', '13500000001', '/avatars/inactive1.jpg', 3, 'INACTIVE',
 DATE_SUB(NOW(), INTERVAL 190 DAY), 3, DATE_SUB(NOW(), INTERVAL 200 DAY)),
('inactive2', '123456', 'inactive2@example.com', '13500000002', '/avatars/inactive2.jpg', 3, 'INACTIVE',
 DATE_SUB(NOW(), INTERVAL 200 DAY), 2, DATE_SUB(NOW(), INTERVAL 220 DAY));

-- 初始化用户组数据
INSERT INTO user_groups (name, description, type)
VALUES ('新注册用户', '最近30天内注册的新用户', 'SYSTEM'),
       ('高频订单用户', '过去60天内有订单的用户', 'SYSTEM'),
       ('活跃用户', '过去30天登录10次以上的用户', 'SYSTEM'),
       ('休眠用户', '超过180天未登录的用户', 'SYSTEM'),
       ('营销活动组', '参与特定营销活动的用户', 'CUSTOM'),
       ('测试用户组', '用于功能测试的内部用户', 'CUSTOM'),
       ('VIP用户组', 'VIP会员用户', 'SYSTEM'),
       ('学生用户组', '学生用户专享优惠', 'CUSTOM');

-- 初始化用户组关联数据
INSERT INTO user_group_relations (user_id, group_id)
SELECT u.id, g.id
FROM users u,
     user_groups g
WHERE (u.username LIKE 'new%' AND g.name = '新注册用户')
   OR (u.username LIKE 'vip%' AND g.name = 'VIP用户组')
   OR (u.username LIKE 'inactive%' AND g.name = '休眠用户')
   OR (u.role_id = 3 AND g.name = '活跃用户' AND u.last_login > DATE_SUB(NOW(), INTERVAL 30 DAY) AND u.login_count >= 5)
   OR (u.id % 3 = 0 AND g.name = '营销活动组')
   OR (u.id % 5 = 0 AND g.name = '测试用户组')
   OR (u.id % 4 = 0 AND g.name = '学生用户组');

-- 清空并重新初始化影厅数据
TRUNCATE TABLE halls;
INSERT INTO halls (hall_name, total_seats, total_rows, total_columns, hall_type, status, price_multiplier)
VALUES
-- 普通厅
('1号厅', 120, 10, 12, 'REGULAR', 'ACTIVE', 1.0),
('2号厅', 108, 9, 12, 'REGULAR', 'ACTIVE', 1.0),
('3号厅', 96, 8, 12, 'REGULAR', 'ACTIVE', 1.0),
-- VIP厅
('4号厅', 80, 8, 10, 'VIP', 'ACTIVE', 1.5),
('5号厅', 72, 8, 9, 'VIP', 'ACTIVE', 1.5),
-- 杜比厅
('6号厅', 150, 10, 15, 'DOLBY', 'ACTIVE', 1.8),
-- IMAX厅
('7号厅', 200, 10, 20, 'IMAX', 'ACTIVE', 2.0);

-- 清空并重新初始化座位数据
TRUNCATE TABLE seats;

-- 为每个影厅生成座位数据
DELIMITER //
CREATE PROCEDURE generate_seats()
BEGIN
    DECLARE hall_id_var INT;
    DECLARE hall_name_var VARCHAR(50);
    DECLARE total_rows_var INT;
    DECLARE total_columns_var INT;
    DECLARE hall_type_var VARCHAR(20);
    DECLARE row_counter INT;
    DECLARE col_counter INT;
    DECLARE seat_type_var VARCHAR(20);
    DECLARE price_multiplier_var DECIMAL(3, 1);
    DECLARE done INT DEFAULT FALSE;

    DECLARE hall_cursor CURSOR FOR
        SELECT id, hall_name, total_rows, total_columns, hall_type FROM halls;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN hall_cursor;

    hall_loop:
    LOOP
        FETCH hall_cursor INTO hall_id_var, hall_name_var, total_rows_var, total_columns_var, hall_type_var;

        IF done THEN
            LEAVE hall_loop;
        END IF;

        SET row_counter = 1;

        WHILE row_counter <= total_rows_var
            DO
                SET col_counter = 1;

                WHILE col_counter <= total_columns_var
                    DO
                    -- 确定座位类型和价格乘数
                    -- 统一对每个影厅的中间核心区域设置为VIP座，价格倍数为1.5
                        IF row_counter BETWEEN CEIL(total_rows_var * 0.4) AND CEIL(total_rows_var * 0.7)
                            AND col_counter BETWEEN CEIL(total_columns_var * 0.3) AND CEIL(total_columns_var * 0.7) THEN
                            SET seat_type_var = 'VIP';
                            SET price_multiplier_var = 1.5; -- 统一设置VIP座价格倍数为1.5
                        ELSE
                            -- 非核心区域根据影厅类型设置不同的座位类型
                            IF hall_type_var = 'REGULAR' THEN
                                SET seat_type_var = 'REGULAR';
                                SET price_multiplier_var = 1.0;
                            ELSEIF hall_type_var = 'VIP' THEN
                                SET seat_type_var = 'VIP';
                                SET price_multiplier_var = 1.5;
                            ELSEIF hall_type_var = 'DOLBY' THEN
                                SET seat_type_var = 'VIP';
                                SET price_multiplier_var = 1.8;
                            ELSEIF hall_type_var = 'IMAX' THEN
                                SET seat_type_var = 'VIP';
                                SET price_multiplier_var = 2.0;
                            END IF;
                        END IF;

                        -- 插入座位数据，座位编号格式为'xx排xx座'
                        INSERT INTO seats (hall_id, seat_number, seat_row, seat_column, seat_type, price_multiplier)
                        VALUES (hall_id_var,
                                CONCAT(row_counter, '排', col_counter, '座'),
                                row_counter,
                                col_counter,
                                seat_type_var,
                                price_multiplier_var);

                        SET col_counter = col_counter + 1;
                    END WHILE;

                SET row_counter = row_counter + 1;
            END WHILE;
    END LOOP;

    CLOSE hall_cursor;
END //
DELIMITER ;

-- 执行存储过程生成座位
CALL generate_seats();

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS generate_seats;

-- 清空并重新初始化电影数据
TRUNCATE TABLE movies;
INSERT INTO movies (title, description, director, actors, genre, duration_minutes, release_date, end_date, poster_url,
                    trailer_url, base_price, status, rating, rating_count, view_count, language, country)
VALUES
-- 动作/科幻类
('复仇者联盟4：终局之战',
 '漫威电影宇宙的史诗级终章，讲述了复仇者联盟为了挽回灭霸消灭宇宙一半生命的惨剧，重新集结并踏上了一场穿越时空的冒险。英雄们将面临前所未有的挑战，为了拯救宇宙，他们必须付出一切。这部电影不仅有震撼的视觉效果和激动人心的战斗场面，还有感人至深的情感故事，完美诠释了英雄主义和自我牺牲的精神。',
 '安东尼·罗素,乔·罗素',
 '小罗伯特·唐尼,克里斯·埃文斯,克里斯·海姆斯沃斯,马克·鲁法洛,斯嘉丽·约翰逊,杰瑞米·雷纳,保罗·路德,布丽·拉尔森,唐·钱德尔,凯伦·吉兰,乔什·布洛林',
 '动作,科幻,冒险', 181, '2019-04-26', '2023-12-31', '/posters/avengers4.jpg', '/trailers/avengers4.mp4', 45.00,
 'NOW_SHOWING', 9.2, 125000, 5000000, '英语', '美国'),

('阿凡达：水之道',
 '《阿凡达：水之道》是詹姆斯·卡梅隆导演的科幻史诗续集，故事发生在潘多拉星球上，讲述了杰克·萨利和奈蒂莉组建家庭后的生活。当一个熟悉的威胁重返潘多拉，萨利一家被迫离开家园，前往潘多拉海洋中的珊瑚礁地区寻求庇护。在这里，他们必须适应水中生活，并与当地的海洋部落建立联系。影片以令人惊叹的视觉效果展现了潘多拉海底世界的壮丽景色，同时探讨了家庭、归属和环境保护等主题。',
 '詹姆斯·卡梅隆', '萨姆·沃辛顿,佐伊·索尔达娜,西格妮·韦弗,凯特·温斯莱特,杰梅奈·克莱门特,史蒂芬·朗', '科幻,冒险,动作',
 192, '2022-12-16', '2023-12-31', '/posters/avatar2.jpg', '/trailers/avatar2.mp4', 55.00, 'NOW_SHOWING', 8.7, 95000,
 3200000, '英语', '美国'),

('星际穿越',
 '《星际穿越》是克里斯托弗·诺兰导演的科幻杰作，讲述了在地球面临生存危机的未来，一组宇航员通过一个神秘的虫洞开始星际旅行，寻找人类新家园的故事。影片深入探讨了时间相对论、黑洞物理学和五维空间等科学概念，同时也是一个关于爱、牺牲和人类求生本能的感人故事。库珀（马修·麦康纳饰）作为宇航员必须在拯救人类和回到自己孩子身边之间做出艰难的选择，而爱可能是跨越时空的唯一力量。诺兰通过壮观的视觉效果和精心设计的情节，创造了一部既有科学深度又充满情感力量的电影杰作。',
 '克里斯托弗·诺兰', '马修·麦康纳,安妮·海瑟薇,杰西卡·查斯坦,比尔·欧文,迈克尔·凯恩,麦肯吉·弗依', '科幻,冒险,剧情', 169,
 '2014-11-07', '2023-12-31', '/posters/interstellar.jpg', '/trailers/interstellar.mp4', 40.00, 'NOW_SHOWING', 9.1,
 76000, 2100000, '英语', '美国'),

('沙丘',
 '《沙丘》改编自弗兰克·赫伯特的同名科幻小说，讲述了在遥远的未来，贵族保罗·厄崔迪家族接受了管理沙漠行星厄拉科斯（又称"沙丘"）的任务。这个星球是珍贵资源香料的唯一来源，能够延长生命并提供超人的思维能力。然而，这是一个充满危险的任务，因为行星上充满了巨大的沙虫、恶劣的环境和当地的弗雷曼人。当一场可怕的阴谋导致保罗的家族遭到背叛，他必须在这个敌对的世界中生存下来，并实现自己作为救世主的命运。导演丹尼斯·维伦纽瓦以壮观的视觉效果和深刻的主题探索，创造了一部宏大的科幻史诗。',
 '丹尼斯·维伦纽瓦', '提莫西·查拉梅,丽贝卡·弗格森,奥斯卡·伊萨克,赞达亚,杰森·莫玛,哈维尔·巴登', '科幻,冒险,剧情', 155,
 '2021-10-22', '2023-12-31', '/posters/dune.jpg', '/trailers/dune.mp4', 48.00, 'NOW_SHOWING', 8.9, 68000, 1900000,
 '英语', '美国'),

-- 动画/家庭类
('疯狂动物城',
 '《疯狂动物城》是一部充满智慧与温情的动画电影，讲述了兔子朱迪·霍普斯从小就梦想成为动物城的一名警察。尽管没有人相信一只兔子能胜任这个工作，但她凭借自己的努力和决心，成功地实现了梦想。然而，初到动物城的朱迪很快发现这个表面和谐的城市暗藏危机——多位食草动物神秘失踪。为了解开这个谜团，朱迪不得不与狡猾的狐狸尼克·王德联手。在调查过程中，他们揭露了一个足以摧毁动物城和平的惊天阴谋。这部电影不仅有精彩的冒险故事和幽默的对白，还通过动物世界的比喻，深刻探讨了偏见、包容和自我实现等社会主题。',
 '拜伦·霍华德,瑞奇·摩尔', '金妮弗·古德温,杰森·贝特曼,伊德里斯·艾尔巴,珍妮·斯蕾特,内特·托伦斯,邦尼·亨特',
 '动画,喜剧,冒险', 108, '2016-03-04', '2023-12-31', '/posters/zootopia.jpg', '/trailers/zootopia.mp4', 28.00,
 'NOW_SHOWING', 8.9, 67000, 2200000, '英语', '美国'),

('玩具总动员4',
 '《玩具总动员4》是皮克斯经典系列的最新续集，讲述了胡迪和他的玩具伙伴们跟随新主人邦妮开始新生活的故事。当邦妮在幼儿园手工课上创造了一个叫"叉叉"的新玩具，并视它为心爱之物时，胡迪主动承担起保护这个不安的新成员的责任。在一次公路旅行中，胡迪与多年前失散的老朋友牧羊女重逢，并发现她已经适应了没有主人的自由生活。面对牧羊女提出的加入她的选择，以及保护叉叉和回到邦妮身边的责任，胡迪必须做出艰难的决定。这部电影不仅延续了系列一贯的冒险和幽默，还深入探讨了目标、责任和人生意义等主题，为这个备受喜爱的系列带来了感人的告别。',
 '乔什·库雷', '汤姆·汉克斯,蒂姆·艾伦,安妮·波茨,托尼·海尔,基努·里维斯,克里斯蒂娜·亨德里克斯', '动画,冒险,喜剧', 100,
 '2019-06-21', '2023-12-31', '/posters/toystory4.jpg', '/trailers/toystory4.mp4', 30.00, 'NOW_SHOWING', 8.5, 58000,
 1800000, '英语', '美国'),

('寻梦环游记',
 '《寻梦环游记》是一部充满墨西哥文化色彩的动画电影，讲述了热爱音乐的小男孩米格尔的奇幻冒险。在一个禁止音乐的家庭中长大的米格尔，梦想成为像偶像德拉库斯一样的音乐家。在亡灵节这天，一连串的意外事件使米格尔穿越到了亡灵世界。在那里，他必须寻找已故的音乐家祖先的祝福才能返回人间，同时也揭开了家族历史中被隐藏的秘密。这部电影以绚丽的色彩和动人的音乐，探索了家庭、记忆和追求梦想的重要性，同时也向墨西哥的亡灵节传统致敬。',
 '李·昂克里奇,阿德里安·莫利纳',
 '安东尼·冈萨雷斯,盖尔·加西亚·贝纳尔,本杰明·布拉特,阿兰娜·乌巴赫,芮妮·维克托,杰米·卡米尔', '动画,冒险,喜剧', 105,
 '2017-11-22', '2023-12-31', '/posters/coco.jpg', '/trailers/coco.mp4', 32.00, 'NOW_SHOWING', 9.0, 72000, 2000000,
 '英语,西班牙语', '美国'),

-- 剧情类
('肖申克的救赎',
 '《肖申克的救赎》是一部经典的剧情片，讲述了银行家安迪·杜弗雷因被错误地指控谋杀妻子及其情人而被判终身监禁的故事。在肖申克监狱的残酷环境中，安迪凭借自己的智慧和坚韧不拔的精神，不仅赢得了狱友的尊重，还成为了监狱长和狱警的财务顾问。他与狱友"红"建立了深厚的友谊，同时秘密策划着自己的越狱计划。经过近二十年的耐心等待和精心准备，安迪最终成功越狱，实现了自由和正义。这部电影通过安迪的故事，深刻探讨了希望、友谊、自由和救赎等永恒主题，被誉为电影史上的经典之作。',
 '弗兰克·德拉邦特', '蒂姆·罗宾斯,摩根·弗里曼,鲍勃·冈顿,威廉姆·赛德勒,克兰西·布朗,吉尔·贝罗斯', '剧情,犯罪', 142,
 '1994-09-23', '2023-12-31', '/posters/shawshank.jpg', '/trailers/shawshank.mp4', 30.00, 'NOW_SHOWING', 9.7, 156000,
 4200000, '英语', '美国'),

('绿皮书',
 '《绿皮书》是一部感人至深的公路电影，基于真实故事改编。故事发生在1962年，意大利裔美国人托尼·利普（维果·莫腾森饰）受雇于非裔古典钢琴家唐·雪莉（马赫沙拉·阿里饰），担任其南方巡演的司机兼保镖。在种族隔离严重的美国南方，这对性格迥异的组合——粗犷直率的托尼和优雅知性的唐博士——必须依靠"绿皮书"（一本为有色人种旅行者提供安全住宿信息的指南）来完成他们的旅程。在旅途中，两人逐渐超越了最初的雇佣关系，建立了真挚的友谊，同时也面对并克服了种族偏见和社会歧视。这部电影以幽默温暖的方式，探讨了种族、阶级和友谊的复杂主题，展现了人性的光辉和社会的进步。',
 '彼得·法雷里', '维果·莫腾森,马赫沙拉·阿里,琳达·卡德里尼,塞巴斯蒂安·马尼斯科,迪米特·D·马里诺夫,迈克·哈顿',
 '剧情,喜剧,传记', 130, '2018-11-16', '2023-12-31', '/posters/greenbook.jpg', '/trailers/greenbook.mp4', 35.00,
 'NOW_SHOWING', 8.9, 65000, 1800000, '英语', '美国'),

('当幸福来敲门',
 '《当幸福来敲门》是一部基于克里斯·加德纳真实经历的励志电影。故事讲述了单身父亲克里斯·加德纳（威尔·史密斯饰）在经历事业失败、妻子离开和经济困境后，仍然坚持抚养年幼的儿子。为了改变命运，他争取到了一家证券公司的无薪实习机会，希望通过自己的努力获得正式职位。在实习期间，克里斯必须面对无家可归、经济拮据等种种困难，但他从未放弃对美好生活的追求和对儿子的责任。这部电影真实展现了一个人在逆境中的坚韧和勇气，以及父爱的伟大，传递了即使在最黑暗的时刻也不应放弃希望的积极信息。',
 '加布里埃莱·穆奇诺', '威尔·史密斯,贾登·史密斯,坦迪·牛顿,布莱恩·豪威,詹姆斯·凯伦,丹·卡斯泰兰尼塔', '剧情,传记', 117,
 '2006-12-15', '2023-12-31', '/posters/pursuit_of_happyness.jpg', '/trailers/pursuit_of_happyness.mp4', 32.00,
 'NOW_SHOWING', 8.8, 70000, 1900000, '英语', '美国'),

-- 爱情/浪漫类
('泰坦尼克号',
 '《泰坦尼克号》是詹姆斯·卡梅隆导演的史诗级爱情灾难片，讲述了发生在1912年的一个跨越阶级的爱情故事。贫穷但自由的艺术家杰克·道森（莱昂纳多·迪卡普里奥饰）在赌博中赢得了泰坦尼克号的船票，在船上他邂逅了上流社会的少女露丝·德威特·布克特（凯特·温斯莱特饰）。尽管露丝已经与富有的卡尔·豪利订婚，但她与杰克之间产生了真挚的爱情。当这艘被称为"永不沉没"的豪华客轮撞上冰山并开始沉没时，杰克和露丝的爱情，以及船上1500多名乘客的生命，都面临着生死考验。这部电影不仅是一个动人的爱情故事，还通过泰坦尼克号的悲剧，展现了人性在灾难面前的勇气、牺牲和尊严。',
 '詹姆斯·卡梅隆', '莱昂纳多·迪卡普里奥,凯特·温斯莱特,比利·赞恩,凯西·贝茨,弗兰西丝·费舍,格洛丽亚·斯图尔特',
 '爱情,灾难,剧情', 194, '1997-12-19', '2023-12-31', '/posters/titanic.jpg', '/trailers/titanic.mp4', 35.00,
 'NOW_SHOWING', 9.4, 98000, 3500000, '英语', '美国'),

('爱乐之城',
 '《爱乐之城》是一部充满音乐与舞蹈的现代爱情故事，讲述了在洛杉矶追求梦想的两个年轻人的相遇与相爱。米娅（艾玛·斯通饰）是一位怀揣演员梦想的咖啡店女服务员，塞巴斯蒂安（瑞恩·高斯林饰）则是一位热爱传统爵士乐并梦想开设自己爵士乐俱乐部的钢琴家。两人在洛杉矶的不同场合多次偶遇，从最初的不屑到相互吸引，他们坠入爱河并互相支持对方的梦想。然而，随着各自事业的发展，他们的关系面临着选择与牺牲的考验。这部电影以华丽的歌舞场景和动人的音乐，展现了梦想、爱情和艺术的美丽与残酷，以及人生中那些必须做出的艰难选择。',
 '达米恩·查泽雷', '瑞恩·高斯林,艾玛·斯通,约翰·传奇,罗丝玛丽·德薇特,芬·维特洛克,J·K·西蒙斯', '爱情,音乐,剧情', 128,
 '2016-12-09', '2023-12-31', '/posters/lalaland.jpg', '/trailers/lalaland.mp4', 38.00, 'NOW_SHOWING', 8.6, 62000,
 1700000, '英语', '美国'),

-- 恐怖/惊悚类
('寂静之地',
 '《寂静之地》是一部创新的恐怖惊悚片，设定在一个后启示录的世界，地球被具有超强听力的外星生物入侵，任何声音都可能导致致命的攻击。故事聚焦于艾伯特一家，他们在这个必须保持绝对安静的世界中求生存。父亲李（约翰·卡拉辛斯基饰）和母亲伊芙琳（艾米莉·布朗特饰）竭尽全力保护他们的孩子，包括聋哑的女儿里根和年幼的儿子马库斯。当伊芙琳怀孕并即将分娩时，家庭面临着前所未有的挑战——如何在不发出声音的情况下生下并抚养一个婴儿。这部电影通过极简的对白和精心设计的声音效果，创造了持续的紧张感和恐怖氛围，同时也是一个关于家庭、牺牲和生存的感人故事。',
 '约翰·卡拉辛斯基', '艾米莉·布朗特,约翰·卡拉辛斯基,米利森特·西蒙兹,诺亚·尤佩,凯德·伍德沃德', '恐怖,惊悚,科幻', 90,
 '2018-04-06', '2023-12-31', '/posters/quiet_place.jpg', '/trailers/quiet_place.mp4', 42.00, 'NOW_SHOWING', 8.5, 55000,
 1600000, '英语', '美国'),

-- 喜剧类
('小丑',
 '《小丑》是一部黑暗而深刻的角色研究电影，讲述了DC漫画中著名反派角色小丑的起源故事。故事设定在1981年的哥谭市，亚瑟·弗莱克（华金·菲尼克斯饰）是一个生活困顿的失败喜剧演员，同时也是一个患有精神疾病的社会边缘人。他在社会的冷漠和残酷对待下，逐渐失去理智，最终转变成为了哥谭市的犯罪王子——小丑。这部电影以写实主义的手法，探讨了精神健康、社会不平等和身份认同等严肃主题，展现了一个普通人如何在极端环境下转变为反社会的罪犯。华金·菲尼克斯凭借对亚瑟复杂心理和身体变化的精湛演绎，获得了奥斯卡最佳男主角奖。',
 '托德·菲利普斯', '华金·菲尼克斯,罗伯特·德尼罗,扎齐·比兹,弗兰西丝·康罗伊,布莱特·卡伦,肖恩·古恩', '剧情,犯罪,惊悚', 122,
 '2019-10-04', '2023-12-31', '/posters/joker.jpg', '/trailers/joker.mp4', 45.00, 'NOW_SHOWING', 8.7, 80000, 2500000,
 '英语', '美国');

-- 清空并重新初始化电影场次数据
TRUNCATE TABLE movie_sessions;

-- 创建存储过程生成未来7天的电影场次
DELIMITER //
CREATE PROCEDURE generate_movie_sessions()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE movie_id_var INT;
    DECLARE hall_id_var INT;
    DECLARE movie_duration INT;
    DECLARE session_date DATE;
    DECLARE session_time DATETIME;
    DECLARE end_time DATETIME;
    DECLARE done INT DEFAULT FALSE;
    DECLARE movie_cursor CURSOR FOR SELECT id, duration_minutes FROM movies WHERE status = 'NOW_SHOWING';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- 循环7天
    WHILE i < 7
        DO
            SET session_date = DATE_ADD(CURDATE(), INTERVAL i DAY);

            -- 为每个影厅安排场次
            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '1号普通厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '10:00', '23:00');

            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '2号普通厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '10:30', '22:30');

            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '3号普通厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '11:00', '23:00');

            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '4号VIP厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '12:00', '23:30');

            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '5号VIP厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '13:00', '23:00');

            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '6号杜比厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '11:30', '23:30');

            SELECT id INTO hall_id_var FROM halls WHERE hall_name = '7号IMAX厅' LIMIT 1;
            CALL schedule_hall_sessions(hall_id_var, session_date, '10:00', '00:00');

            SET i = i + 1;
        END WHILE;
END //
DELIMITER ;

-- 创建存储过程为每个影厅安排场次
DELIMITER //
CREATE PROCEDURE schedule_hall_sessions(IN hall_id_var INT, IN session_date DATE, IN start_time_str VARCHAR(5),
                                        IN end_time_str VARCHAR(5))
BEGIN
    DECLARE cur_time DATETIME;
    DECLARE end_datetime DATETIME;
    DECLARE movie_id_var INT;
    DECLARE movie_duration INT;
    DECLARE movie_end_time DATETIME;
    DECLARE movie_count INT;
    DECLARE movie_index INT;
    DECLARE done INT DEFAULT FALSE;
    DECLARE movie_cursor CURSOR FOR
        SELECT id, duration_minutes
        FROM movies
        WHERE status = 'NOW_SHOWING'
        ORDER BY RAND();
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- 获取电影总数
    SELECT COUNT(*) INTO movie_count FROM movies WHERE status = 'NOW_SHOWING';

    SET cur_time = CONCAT(session_date, ' ', start_time_str);
    IF end_time_str = '24:00' OR end_time_str = '00:00' THEN
        SET end_datetime = DATE_ADD(CONCAT(session_date, ' 00:00'), INTERVAL 1 DAY);
    ELSE
        SET end_datetime = CONCAT(session_date, ' ', end_time_str);
    END IF;

    -- 创建临时表存储所有电影
    DROP TEMPORARY TABLE IF EXISTS temp_movies;
    CREATE TEMPORARY TABLE temp_movies
    (
        idx      INT AUTO_INCREMENT PRIMARY KEY,
        movie_id INT,
        duration INT
    );

    -- 将所有电影随机排序后插入临时表
    INSERT INTO temp_movies (movie_id, duration)
    SELECT id, duration_minutes
    FROM movies
    WHERE status = 'NOW_SHOWING'
    ORDER BY RAND();

    SET movie_index = 1;

    -- 循环安排场次，确保每个时间段随机分配一部电影
    time_loop:
    WHILE cur_time < end_datetime
        DO
            -- 获取下一部电影
            SELECT movie_id, duration
            INTO movie_id_var, movie_duration
            FROM temp_movies
            WHERE idx = movie_index;

            -- 如果已经用完所有电影，重新开始
            IF movie_index > movie_count THEN
                SET movie_index = 1;
                -- 重新随机排序电影
                TRUNCATE TABLE temp_movies;
                INSERT INTO temp_movies (movie_id, duration)
                SELECT id, duration_minutes
                FROM movies
                WHERE status = 'NOW_SHOWING'
                ORDER BY RAND();

                -- 获取新的电影
                SELECT movie_id, duration
                INTO movie_id_var, movie_duration
                FROM temp_movies
                WHERE idx = movie_index;
            END IF;

            -- 计算电影结束时间
            SET movie_end_time = DATE_ADD(cur_time, INTERVAL movie_duration + 20 MINUTE);
            -- 加20分钟清场时间

            -- 如果电影结束时间超过营业结束时间，结束循环
            IF movie_end_time > end_datetime THEN
                LEAVE time_loop;
            END IF;

            -- 插入场次数据
            INSERT INTO movie_sessions (movie_id, hall_id, session_time, end_time)
            VALUES (movie_id_var, hall_id_var, cur_time, DATE_ADD(cur_time, INTERVAL movie_duration MINUTE));

            -- 更新当前时间为电影结束时间
            SET cur_time = movie_end_time;

            -- 移动到下一部电影
            SET movie_index = movie_index + 1;
        END WHILE;

    -- 删除临时表
    DROP TEMPORARY TABLE IF EXISTS temp_movies;
END //
DELIMITER ;

-- 执行存储过程生成电影场次
CALL generate_movie_sessions();

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS generate_movie_sessions;
DROP PROCEDURE IF EXISTS schedule_hall_sessions;

-- 清空并重新初始化座位场次关联数据
TRUNCATE TABLE seats_sessions;

-- 为每个场次的每个座位生成座位场次关联数据，全部初始化为'AVAILABLE'
INSERT INTO seats_sessions (seat_id, session_id, status)
SELECT s.id, ms.id, 'AVAILABLE'
FROM seats s
         JOIN movie_sessions ms ON s.hall_id = ms.hall_id
WHERE s.is_deleted = 0
  AND ms.is_deleted = 0;

-- ==================== 数据库优化完成提示 ====================
SELECT '电影购票系统数据库优化完成！' as message;
SELECT '已完成以下优化：' as optimizations;
SELECT '- 创建了必要的索引以提高查询性能' as optimization1;
SELECT '- 初始化了用户数据' as optimization2;
SELECT '- 创建了7个不同类型的影厅' as optimization3;
SELECT '- 更新了座位编号格式为"xx排xx座"' as optimization4;
SELECT '- 添加了丰富的电影数据' as optimization5;
SELECT '- 生成了未来7天的电影场次' as optimization6;
SELECT '- 初始化了座位场次关联数据' as optimization7;
SELECT '- 随机分配了用户到不同的用户组' as optimization8;

SET FOREIGN_KEY_CHECKS = 1;