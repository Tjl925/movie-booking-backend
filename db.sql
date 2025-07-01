-- ==================== 电影购票系统数据库脚本 ====================
-- 创建数据库
CREATE DATABASE IF NOT EXISTS movie_booking_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE movie_booking_system;

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS movie_sessions;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS seats_sessions;
DROP TABLE IF EXISTS halls;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS operation_logs;
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

-- 创建权限表
CREATE TABLE permissions
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    name         VARCHAR(100) NOT NULL UNIQUE COMMENT '权限名称',
    display_name VARCHAR(100) NOT NULL COMMENT '权限显示名称',
    description  VARCHAR(255) COMMENT '权限描述',
    resource     VARCHAR(50)  NOT NULL COMMENT '资源类型',
    action       VARCHAR(50)  NOT NULL COMMENT '操作类型',
    created_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_resource_action (resource, action),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='权限表';

-- 创建角色权限关联表
CREATE TABLE role_permissions
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色权限关联表';

-- 创建用户表
CREATE TABLE users
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码',
    email       VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone       VARCHAR(20) UNIQUE COMMENT '手机号',
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
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 创建用户组表
CREATE TABLE user_groups
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户组ID',
    name        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户组名称',
    description VARCHAR(255) COMMENT '用户组描述',
    type       ENUM ('SYSTEM', 'CUSTOM') DEFAULT 'CUSTOM' COMMENT '用户组类型',
    created_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户组表';

-- 创建用户组关系表
CREATE TABLE user_group_relations
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    group_id    BIGINT NOT NULL COMMENT '用户组ID',
    created_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1) DEFAULT 0 COMMENT '是否删除',
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
    hall_type        ENUM ('REGULAR', 'VIP', 'IMAX', 'DOLBY')   DEFAULT 'REGULAR' COMMENT '影厅类型',
    status           ENUM ('ACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE' COMMENT '状态',
    price_multiplier DECIMAL(3, 2)                                             DEFAULT 1.00 COMMENT '价格倍数，根据影厅类型计算',
    created_at       TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)                                 DEFAULT 0 COMMENT '是否删除',
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
    seat_type        ENUM ('REGULAR', 'VIP')                                   DEFAULT 'REGULAR' COMMENT '座位类型',
    price_multiplier DECIMAL(3, 2)                                             DEFAULT 1.00 COMMENT '价格倍数，根据座位类型计算',
    created_at       TIMESTAMP                                                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP                                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)                                                DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_hall_seat (hall_id, seat_number),
    INDEX idx_hall_id (hall_id),
    INDEX idx_seat_type (seat_type),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_seat_hall FOREIGN KEY (hall_id) REFERENCES halls (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='座位表';

-- ==================== 场次管理模块 ====================

-- 创建电影场次表
CREATE TABLE movie_sessions
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '场次ID',
    movie_id         BIGINT   NOT NULL COMMENT '电影ID',
    hall_id          BIGINT   NOT NULL COMMENT '影厅ID',
    session_time     DATETIME NOT NULL COMMENT '开始时间',
    end_time         DATETIME NOT NULL COMMENT '结束时间',
    created_at       TIMESTAMP                                            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       TIMESTAMP                                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT(1)                                           DEFAULT 0 COMMENT '是否删除',
    INDEX idx_movie_id (movie_id),
    INDEX idx_hall_id (hall_id),
    INDEX idx_session_time (session_time),
    INDEX idx_movie_time (movie_id, session_time),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_session_movie FOREIGN KEY (movie_id) REFERENCES movies (id),
    CONSTRAINT fk_session_hall FOREIGN KEY (hall_id) REFERENCES halls (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='电影场次表';

-- 创建座位场次关联表
CREATE TABLE seats_sessions
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    seat_id       BIGINT NOT NULL COMMENT '座位ID',
    session_id    BIGINT NOT NULL COMMENT '场次ID',
    status        ENUM ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE' COMMENT '状态',
    created_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT(1) DEFAULT 0 COMMENT '是否删除',
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
    cancel_time    DATETIME COMMENT '取消时间',
    cancel_reason  VARCHAR(255) COMMENT '取消原因',
    created_at     TIMESTAMP                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     TIMESTAMP                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT(1)                                                     DEFAULT 0 COMMENT '是否删除',
    INDEX idx_order_number (order_number),
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_orders_session FOREIGN KEY (session_id) REFERENCES movie_sessions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单表';

-- 创建订单项表
CREATE TABLE order_items
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id    BIGINT         NOT NULL COMMENT '订单ID',
    seat_id     BIGINT         NOT NULL COMMENT '座位ID',
    price       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    created_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_order_id (order_id),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_orderitem_seat FOREIGN KEY (seat_id) REFERENCES seats (id)
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

-- ==================== 操作日志模块 ====================

-- 创建操作日志表
CREATE TABLE operation_logs
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id         BIGINT COMMENT '操作用户ID',
    username        VARCHAR(50) COMMENT '操作用户名',
    operation       VARCHAR(100) NOT NULL COMMENT '操作类型',
    resource        VARCHAR(50)  NOT NULL COMMENT '操作资源',
    resource_id     BIGINT COMMENT '资源ID',
    description     TEXT COMMENT '操作描述',
    ip_address      VARCHAR(45) COMMENT 'IP地址',
    user_agent      TEXT COMMENT '用户代理',
    request_url     VARCHAR(500) COMMENT '请求URL',
    request_method  VARCHAR(10) COMMENT '请求方法',
    request_params  TEXT COMMENT '请求参数',
    response_status INT COMMENT '响应状态码',
    execution_time  BIGINT COMMENT '执行时间(毫秒)',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_resource (resource),
    INDEX idx_created_at (created_at),
    INDEX idx_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';

-- ==================== 外键约束 ====================

-- 添加外键约束
ALTER TABLE users
    ADD CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles (id);
ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles (id);
ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions (id);
ALTER TABLE seats
    ADD CONSTRAINT fk_seats_hall_id FOREIGN KEY (hall_id) REFERENCES halls (id);
ALTER TABLE movie_sessions
    ADD CONSTRAINT fk_movie_sessions_movie_id FOREIGN KEY (movie_id) REFERENCES movies (id);
ALTER TABLE movie_sessions
    ADD CONSTRAINT fk_movie_sessions_hall_id FOREIGN KEY (hall_id) REFERENCES halls (id);
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_session_id FOREIGN KEY (session_id) REFERENCES movie_sessions (id);
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order_id FOREIGN KEY (order_id) REFERENCES orders (id);
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_user_id FOREIGN KEY (user_id) REFERENCES users (id);

-- ==================== 视图定义 ====================

-- 创建电影详情视图
CREATE OR REPLACE VIEW v_movie_details AS
SELECT m.id,
       m.title,
       m.description,
       m.director,
       m.actors,
       m.genre,
       m.duration_minutes,
       m.release_date,
       m.end_date,
       m.poster_url,
       m.trailer_url,
       m.base_price,
       m.status,
       m.rating,
       m.rating_count,
       m.view_count,
       m.language,
       m.country,
       m.created_at,
       m.updated_at,
       COUNT(DISTINCT ms.id) as session_count,
       COUNT(DISTINCT o.id)  as order_count,
       SUM(o.total_amount)   as total_revenue
FROM movies m
         LEFT JOIN movie_sessions ms ON m.id = ms.movie_id AND ms.is_deleted = 0
         LEFT JOIN orders o ON ms.id = o.session_id AND o.is_deleted = 0 AND o.status = 'PAID'
WHERE m.is_deleted = 0
GROUP BY m.id;

-- 创建用户订单视图
CREATE OR REPLACE VIEW v_user_orders AS
SELECT o.id,
       o.order_number,
       o.user_id,
       u.username,
       u.email,
       u.phone,
       o.session_id,
       ms.session_time,
       ms.end_time,
       m.title      as movie_title,
       m.poster_url as movie_poster,
       h.hall_name,
       o.seat_numbers,
       o.ticket_count,
       o.total_amount,
       o.status,
       o.payment_method,
       o.payment_time,
       o.created_at,
       p.payment_status,
       p.transaction_id
FROM orders o
         JOIN users u ON o.user_id = u.id
         JOIN movie_sessions ms ON o.session_id = ms.id
         JOIN movies m ON ms.movie_id = m.id
         JOIN halls h ON ms.hall_id = h.id
         LEFT JOIN payments p ON o.id = p.order_id
WHERE o.is_deleted = 0
  AND u.is_deleted = 0;

-- 创建场次座位状态视图
CREATE OR REPLACE VIEW v_session_seats AS
SELECT ms.id      as session_id,
       ms.movie_id,
       ms.hall_id,
       ms.session_time,
       ms.end_time,
       s.id       as seat_id,
       s.seat_number,
       s.seat_row,
       s.seat_column,
       s.seat_type,
       s.price_multiplier,
       CASE
           WHEN o.id IS NOT NULL AND o.status IN ('PAID', 'COMPLETED') THEN 'OCCUPIED'
           WHEN o.id IS NOT NULL AND o.status = 'PENDING' THEN 'RESERVED'
           END    as current_status,
       o.id       as order_id,
       o.order_number,
       u.username as booked_by
FROM movie_sessions ms
         JOIN seats s ON ms.hall_id = s.hall_id
         LEFT JOIN orders o ON ms.id = o.session_id
    AND FIND_IN_SET(s.seat_number, o.seat_numbers) > 0
    AND o.is_deleted = 0
         LEFT JOIN users u ON o.user_id = u.id
WHERE ms.is_deleted = 0
  AND s.is_deleted = 0;

-- 创建销售统计视图
CREATE OR REPLACE VIEW v_sales_statistics AS
SELECT DATE(o.created_at)        as sale_date,
       m.id                      as movie_id,
       m.title                   as movie_title,
       m.genre,
       COUNT(DISTINCT o.id)      as order_count,
       COUNT(DISTINCT o.user_id) as customer_count,
       SUM(o.ticket_count)       as ticket_count,
       SUM(o.total_amount)       as total_revenue,
       AVG(o.total_amount)       as avg_order_amount
FROM orders o
         JOIN movie_sessions ms ON o.session_id = ms.id
         JOIN movies m ON ms.movie_id = m.id
WHERE o.status = 'PAID'
  AND o.is_deleted = 0
GROUP BY DATE(o.created_at), m.id, m.title, m.genre;

-- ==================== 初始化数据 ====================

-- 插入角色数据
INSERT INTO roles (name, display_name, description)
VALUES ('SUPER_ADMIN', '系统管理员', '拥有系统所有权限，包括角色管理'),
       ('ADMIN', '管理员', '拥有用户、订单、电影等管理权限'),
       ('USER', '普通用户', '拥有基本的浏览和购票权限');

-- 插入权限数据
INSERT INTO permissions (name, display_name, description, resource, action)
VALUES
-- 用户管理权限
('user:view', '查看用户', '查看用户信息', 'user', 'view'),
('user:create', '创建用户', '创建新用户', 'user', 'create'),
('user:update', '更新用户', '更新用户信息', 'user', 'update'),
('user:delete', '删除用户', '删除用户', 'user', 'delete'),
('user:status', '管理用户状态', '启用/禁用用户', 'user', 'status'),
('user:role:manage', '角色管理', '管理用户角色', 'user', 'role:manage'),

-- 电影管理权限
('movie:view', '查看电影', '查看电影信息', 'movie', 'view'),
('movie:create', '创建电影', '创建新电影', 'movie', 'create'),
('movie:update', '更新电影', '更新电影信息', 'movie', 'update'),
('movie:delete', '删除电影', '删除电影', 'movie', 'delete'),
('movie:status', '管理电影状态', '上架/下架电影', 'movie', 'status'),

-- 影厅管理权限
('hall:view', '查看影厅', '查看影厅信息', 'hall', 'view'),
('hall:create', '创建影厅', '创建新影厅', 'hall', 'create'),
('hall:update', '更新影厅', '更新影厅信息', 'hall', 'update'),
('hall:delete', '删除影厅', '删除影厅', 'hall', 'delete'),

-- 场次管理权限
('session:view', '查看场次', '查看场次信息', 'session', 'view'),
('session:create', '创建场次', '创建新场次', 'session', 'create'),
('session:update', '更新场次', '更新场次信息', 'session', 'update'),
('session:delete', '删除场次', '删除场次', 'session', 'delete'),

-- 订单管理权限
('order:view', '查看订单', '查看订单信息', 'order', 'view'),
('order:create', '创建订单', '创建新订单', 'order', 'create'),
('order:update', '更新订单', '更新订单信息', 'order', 'update'),
('order:delete', '删除订单', '删除订单', 'order', 'delete'),
('order:refund', '订单退款', '处理订单退款', 'order', 'refund'),

-- 支付管理权限
('payment:view', '查看支付', '查看支付信息', 'payment', 'view'),
('payment:process', '处理支付', '处理支付流程', 'payment', 'process'),
('payment:refund', '处理退款', '处理退款流程', 'payment', 'refund'),

-- 系统管理权限
('system:config', '系统配置', '管理系统配置', 'system', 'config'),
('system:log', '查看日志', '查看系统日志', 'system', 'log'),
('system:backup', '数据备份', '备份系统数据', 'system', 'backup');

-- 插入角色权限关联数据
-- 系统管理员拥有所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id
FROM permissions;

-- 管理员拥有管理权限（除角色管理和系统管理外）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id
FROM permissions
WHERE name NOT IN ('user:role:manage', 'system:config', 'system:log', 'system:backup');

-- 普通用户拥有基本权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id
FROM permissions
WHERE name IN ('movie:view', 'session:view', 'order:view', 'order:create', 'payment:process');

-- 插入系统管理员用户
INSERT INTO users (username, password, email, phone, role_id, status, login_count)
VALUES ('superadmin', 'admin123', 'superadmin@movie.com',
        '13800138000', 1, 'ACTIVE', 0);

-- 插入管理员用户
INSERT INTO users (username, password, email, phone, role_id, status, login_count)
VALUES ('admin', 'admin123', 'admin@movie.com', '13800138001', 2,
        'ACTIVE', 0);

-- 插入测试用户
INSERT INTO users (username, password, email, phone, role_id, status, login_count)
VALUES ('testuser', '123456', 'test@movie.com', '13800138002', 3,
        'ACTIVE', 0),
       ('staff', '123456', 'staff@movie.com', '13800138003', 3,
        'ACTIVE', 0);

-- 插入用户组数据
INSERT INTO user_groups (name, description, type)
VALUES ('新注册用户', '最近30天内注册的新用户', 'SYSTEM'),
       ('高频订单用户', '过去60天内有订单的用户', 'SYSTEM'),
       ('活跃用户', '过去30天登录10次以上的用户', 'SYSTEM'),
       ('休眠用户', '超过180天未登录的用户', 'SYSTEM'),
       ('营销活动组', '参与特定营销活动的用户', 'CUSTOM'),
       ('测试用户组', '用于功能测试的内部用户', 'CUSTOM');

-- 插入影厅数据
INSERT INTO halls (hall_name, total_seats, total_rows, total_columns, hall_type, status, price_multiplier)
VALUES ('1号厅', 120, 12, 10, 'REGULAR', 'ACTIVE', 1.0),
       ('2号厅', 80, 8, 10, 'VIP', 'ACTIVE', 1.5),
       ('IMAX厅', 200, 10, 20, 'IMAX', 'ACTIVE', 2.0),
       ('杜比厅', 150, 10, 15, 'DOLBY', 'ACTIVE', 1.8);

-- 为每个影厅生成座位数据
-- 1号厅：12排10列
INSERT INTO seats (hall_id, seat_number, seat_row, seat_column, seat_type, price_multiplier)
SELECT 1,
       CONCAT(LPAD(row_num, 2, '0'), LPAD(col_num, 2, '0')),
       row_num,
       col_num,
       CASE
           WHEN row_num BETWEEN 5 AND 8 AND col_num BETWEEN 3 AND 8 THEN 'VIP'
           ELSE 'REGULAR'
       END,
       CASE
           WHEN row_num BETWEEN 5 AND 8 AND col_num BETWEEN 3 AND 8 THEN 1.5
           ELSE 1.0
       END
FROM (SELECT a.N as row_num, b.N as col_num
      FROM (SELECT 1 as N
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9
            UNION
            SELECT 10
            UNION
            SELECT 11
            UNION
            SELECT 12) a
               CROSS JOIN (SELECT 1 as N
                           UNION
                           SELECT 2
                           UNION
                           SELECT 3
                           UNION
                           SELECT 4
                           UNION
                           SELECT 5
                           UNION
                           SELECT 6
                           UNION
                           SELECT 7
                           UNION
                           SELECT 8
                           UNION
                           SELECT 9
                           UNION
                           SELECT 10) b) seat_combinations;

-- 2号厅：8排10列
INSERT INTO seats (hall_id, seat_number, seat_row, seat_column, seat_type, price_multiplier)
SELECT 2,
       CONCAT(LPAD(row_num, 2, '0'), LPAD(col_num, 2, '0')),
       row_num,
       col_num,
       CASE
           WHEN row_num BETWEEN 3 AND 6 AND col_num BETWEEN 3 AND 8 THEN 'VIP'
           ELSE 'REGULAR'
       END,
       CASE
           WHEN row_num BETWEEN 3 AND 6 AND col_num BETWEEN 3 AND 8 THEN 1.5
           ELSE 1.0
       END
FROM (SELECT a.N as row_num, b.N as col_num
      FROM (SELECT 1 as N
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8) a
               CROSS JOIN (SELECT 1 as N
                           UNION
                           SELECT 2
                           UNION
                           SELECT 3
                           UNION
                           SELECT 4
                           UNION
                           SELECT 5
                           UNION
                           SELECT 6
                           UNION
                           SELECT 7
                           UNION
                           SELECT 8
                           UNION
                           SELECT 9
                           UNION
                           SELECT 10) b) seat_combinations;

-- IMAX厅：10排20列
INSERT INTO seats (hall_id, seat_number, seat_row, seat_column, seat_type, price_multiplier)
SELECT 3,
       CONCAT(LPAD(row_num, 2, '0'), LPAD(col_num, 2, '0')),
       row_num,
       col_num,
       CASE
           WHEN row_num BETWEEN 4 AND 7 AND col_num BETWEEN 6 AND 15 THEN 'VIP'
           ELSE 'REGULAR'
       END,
       CASE
           WHEN row_num BETWEEN 4 AND 7 AND col_num BETWEEN 6 AND 15 THEN 1.5
           ELSE 1.0
       END
FROM (SELECT a.N as row_num, b.N as col_num
      FROM (SELECT 1 as N
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9
            UNION
            SELECT 10) a
               CROSS JOIN (SELECT 1 as N
                           UNION
                           SELECT 2
                           UNION
                           SELECT 3
                           UNION
                           SELECT 4
                           UNION
                           SELECT 5
                           UNION
                           SELECT 6
                           UNION
                           SELECT 7
                           UNION
                           SELECT 8
                           UNION
                           SELECT 9
                           UNION
                           SELECT 10
                           UNION
                           SELECT 11
                           UNION
                           SELECT 12
                           UNION
                           SELECT 13
                           UNION
                           SELECT 14
                           UNION
                           SELECT 15
                           UNION
                           SELECT 16
                           UNION
                           SELECT 17
                           UNION
                           SELECT 18
                           UNION
                           SELECT 19
                           UNION
                           SELECT 20) b) seat_combinations;

-- 杜比厅：10排15列
INSERT INTO seats (hall_id, seat_number, seat_row, seat_column, seat_type, price_multiplier)
SELECT 4,
       CONCAT(LPAD(row_num, 2, '0'), LPAD(col_num, 2, '0')),
       row_num,
       col_num,
       CASE
           WHEN row_num BETWEEN 4 AND 7 AND col_num BETWEEN 5 AND 11 THEN 'VIP'
           ELSE 'REGULAR'
       END,
       CASE
           WHEN row_num BETWEEN 4 AND 7 AND col_num BETWEEN 5 AND 11 THEN 1.5
           ELSE 1.0
       END
FROM (SELECT a.N as row_num, b.N as col_num
      FROM (SELECT 1 as N
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9
            UNION
            SELECT 10) a
               CROSS JOIN (SELECT 1 as N
                           UNION
                           SELECT 2
                           UNION
                           SELECT 3
                           UNION
                           SELECT 4
                           UNION
                           SELECT 5
                           UNION
                           SELECT 6
                           UNION
                           SELECT 7
                           UNION
                           SELECT 8
                           UNION
                           SELECT 9
                           UNION
                           SELECT 10
                           UNION
                           SELECT 11
                           UNION
                           SELECT 12
                           UNION
                           SELECT 13
                           UNION
                           SELECT 14
                           UNION
                           SELECT 15) b) seat_combinations;

-- 插入电影数据
INSERT INTO movies (title, description, director, actors, genre, duration_minutes, release_date, end_date, poster_url,
                    trailer_url, base_price, status, rating, rating_count, view_count, language, country)
VALUES ('复仇者联盟4：终局之战', '漫威电影宇宙的史诗级终章，超级英雄们将面对终极挑战。', '安东尼·罗素',
        '小罗伯特·唐尼,克里斯·埃文斯,克里斯·海姆斯沃斯', '动作,科幻,冒险', 181, '2019-04-26', '2019-07-26',
        '/posters/avengers4.jpg', '/trailers/avengers4.mp4', 45.00, 'NOW_SHOWING', 9.2, 125000, 5000000, '英语',
        '美国'),
       ('泰坦尼克号', '经典爱情灾难片，讲述杰克和露丝在泰坦尼克号上的浪漫爱情故事。', '詹姆斯·卡梅隆',
        '莱昂纳多·迪卡普里奥,凯特·温斯莱特', '爱情,灾难,剧情', 194, '1997-12-19', '1998-03-19', '/posters/titanic.jpg',
        '/trailers/titanic.mp4', 35.00, 'NOW_SHOWING', 9.4, 98000, 3500000, '英语', '美国'),
       ('阿凡达', '科幻史诗巨作，讲述人类在潘多拉星球上的冒险故事。', '詹姆斯·卡梅隆', '萨姆·沃辛顿,佐伊·索尔达娜',
        '科幻,冒险,动作', 162, '2009-12-18', '2010-03-18', '/posters/avatar.jpg', '/trailers/avatar.mp4', 50.00,
        'NOW_SHOWING', 8.8, 89000, 2800000, '英语', '美国'),
       ('星际穿越', '诺兰执导的科幻巨作，探索宇宙奥秘和人类情感。', '克里斯托弗·诺兰', '马修·麦康纳,安妮·海瑟薇',
        '科幻,冒险,剧情', 169, '2014-11-07', '2015-02-07', '/posters/interstellar.jpg', '/trailers/interstellar.mp4',
        40.00, 'NOW_SHOWING', 9.1, 76000, 2100000, '英语', '美国'),
       ('肖申克的救赎', '经典励志剧情片，讲述希望和救赎的永恒主题。', '弗兰克·德拉邦特', '蒂姆·罗宾斯,摩根·弗里曼',
        '剧情,犯罪', 142, '1994-09-23', '1994-12-23', '/posters/shawshank.jpg', '/trailers/shawshank.mp4', 30.00,
        'NOW_SHOWING', 9.7, 156000, 4200000, '英语', '美国'),
       ('功夫熊猫', '梦工厂动画经典，讲述熊猫阿宝的功夫成长之路。', '马克·奥斯本', '杰克·布莱克,安吉丽娜·朱莉',
        '动画,喜剧,动作', 95, '2008-06-06', '2008-09-06', '/posters/kungfu_panda.jpg', '/trailers/kungfu_panda.mp4',
        25.00, 'NOW_SHOWING', 8.6, 45000, 1800000, '英语', '美国'),
       ('疯狂动物城', '迪士尼动画佳作，讲述兔子朱迪和狐狸尼克的破案故事。', '拜伦·霍华德', '金妮弗·古德温,杰森·贝特曼',
        '动画,喜剧,冒险', 108, '2016-03-04', '2016-06-04', '/posters/zootopia.jpg', '/trailers/zootopia.mp4', 28.00,
        'NOW_SHOWING', 8.9, 67000, 2200000, '英语', '美国'),
       ('速度与激情10', '动作冒险大片，多米尼克·托莱多家族的终极冒险。', '路易斯·莱特里尔', '范·迪塞尔,米歇尔·罗德里格兹',
        '动作,冒险,犯罪', 141, '2023-05-19', '2023-08-19', '/posters/fast10.jpg', '/trailers/fast10.mp4', 55.00,
        'NOW_SHOWING', 7.8, 34000, 1200000, '英语', '美国');

-- 插入场次数据（未来7天的场次）
INSERT INTO movie_sessions (movie_id, hall_id, session_time, end_time)
SELECT m.id,
       h.id,
       DATE_ADD(NOW(), INTERVAL day_offset DAY) + INTERVAL hour_offset HOUR,
       DATE_ADD(NOW(), INTERVAL day_offset DAY) + INTERVAL hour_offset HOUR + INTERVAL m.duration_minutes MINUTE
FROM movies m
         CROSS JOIN halls h
         CROSS JOIN (SELECT 0 as day_offset
                     UNION
                     SELECT 1
                     UNION
                     SELECT 2
                     UNION
                     SELECT 3
                     UNION
                     SELECT 4
                     UNION
                     SELECT 5
                     UNION
                     SELECT 6) days
         CROSS JOIN (SELECT 14 as hour_offset
                     UNION
                     SELECT 15
                     UNION
                     SELECT 16
                     UNION
                     SELECT 18
                     UNION
                     SELECT 19
                     UNION
                     SELECT 20) hours
WHERE m.status = 'NOW_SHOWING'
  AND h.status = 'ACTIVE'
ORDER BY day_offset, hour_offset, m.id, h.id;

-- 为每个场次的每个座位生成座位场次关联数据
INSERT INTO seats_sessions (seat_id, session_id, status)
SELECT s.id, ms.id, 'AVAILABLE'
FROM seats s
         CROSS JOIN movie_sessions ms
WHERE s.hall_id = ms.hall_id
  AND s.is_deleted = 0
  AND ms.is_deleted = 0;

-- ==================== 存储过程 ====================

-- 创建生成订单号的存储过程
DELIMITER //
CREATE PROCEDURE GenerateOrderNumber(OUT order_number VARCHAR(50))
BEGIN
    DECLARE current_date_str VARCHAR(8);
    DECLARE sequence_num INT;

    SET current_date_str = DATE_FORMAT(NOW(), '%Y%m%d');

    -- 获取当天的订单数量
    SELECT COALESCE(MAX(SUBSTRING(order_number, 9)), 0) + 1
    INTO sequence_num
    FROM orders
    WHERE order_number LIKE CONCAT(current_date_str, '%')
      AND is_deleted = 0;

    SET order_number = CONCAT(current_date_str, LPAD(sequence_num, 6, '0'));
END //
DELIMITER ;

-- 创建计算座位价格的存储过程
DELIMITER //
CREATE PROCEDURE CalculateSeatPrice(
    IN p_session_id BIGINT,
    IN p_seat_number VARCHAR(20),
    OUT p_price DECIMAL(10, 2)
)
BEGIN
    DECLARE base_price DECIMAL(10, 2);
    DECLARE hall_price_multiplier DECIMAL(3, 2);
    DECLARE seat_price_multiplier DECIMAL(3, 2);

    -- 获取电影基础价格
    SELECT m.base_price
    INTO base_price
    FROM movie_sessions ms
             JOIN movies m ON ms.movie_id = m.id
    WHERE ms.id = p_session_id
      AND ms.is_deleted = 0;

    -- 获取影厅价格倍数
    SELECT h.price_multiplier
    INTO hall_price_multiplier
    FROM movie_sessions ms
             JOIN halls h ON ms.hall_id = h.id
    WHERE ms.id = p_session_id
      AND ms.is_deleted = 0;

    -- 获取座位价格倍数
    SELECT s.price_multiplier
    INTO seat_price_multiplier
    FROM movie_sessions ms
             JOIN seats s ON s.hall_id = ms.hall_id
    WHERE ms.id = p_session_id
      AND s.seat_number = p_seat_number
      AND s.is_deleted = 0;

    SET p_price = base_price * hall_price_multiplier * seat_price_multiplier;
END //
DELIMITER ;

-- ==================== 触发器 ====================

-- 创建订单状态更新触发器
DELIMITER //
CREATE TRIGGER tr_orders_status_update
    AFTER UPDATE
    ON orders
    FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        INSERT INTO operation_logs (user_id, operation, resource, resource_id, description)
        VALUES (NEW.user_id,
                CONCAT('订单状态更新: ', OLD.status, ' -> ', NEW.status),
                'order',
                NEW.id,
                CONCAT('订单号: ', NEW.order_number, ' 状态从 ', OLD.status, ' 更新为 ', NEW.status));
    END IF;
END //
DELIMITER ;

-- 创建支付状态更新触发器
DELIMITER //
CREATE TRIGGER tr_payments_status_update
    AFTER UPDATE
    ON payments
    FOR EACH ROW
BEGIN
    IF NEW.payment_status != OLD.payment_status THEN
        INSERT INTO operation_logs (user_id, operation, resource, resource_id, description)
        VALUES (NEW.user_id,
                CONCAT('支付状态更新: ', OLD.payment_status, ' -> ', NEW.payment_status),
                'payment',
                NEW.id,
                CONCAT('订单ID: ', NEW.order_id, ' 支付状态从 ', OLD.payment_status, ' 更新为 ', NEW.payment_status));
    END IF;
END //
DELIMITER ;

-- ==================== 索引优化 ====================

-- 创建复合索引以提高查询性能
CREATE INDEX idx_orders_user_status ON orders (user_id, status);
CREATE INDEX idx_orders_session_status ON orders (session_id, status);
CREATE INDEX idx_movie_sessions_movie_status ON movie_sessions (movie_id);
CREATE INDEX idx_movie_sessions_time ON movie_sessions (session_time);
CREATE INDEX idx_payments_order_status ON payments (order_id, payment_status);
CREATE INDEX idx_users_role_status ON users (role_id, status);

-- 创建全文索引用于电影搜索
CREATE FULLTEXT INDEX ft_movies_search ON movies (title, description, director, actors, genre);

-- ==================== 数据库完成提示 ====================
SELECT '电影购票系统数据库创建完成！' as message;
SELECT '包含以下功能模块：' as modules;
SELECT '- 用户管理（用户、角色、权限）' as module1;
SELECT '- 电影管理（电影信息、影厅、座位）' as module2;
SELECT '- 场次管理（电影场次、时间安排）' as module3;
SELECT '- 订单管理（订单、订单项、支付）' as module4;
SELECT '- 操作日志（系统操作记录）' as module5;
SELECT '- 视图和存储过程（数据查询和业务逻辑）' as module6;