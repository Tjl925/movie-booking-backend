# 电影购票系统后端

## 项目简介

这是一个基于Spring Boot + MyBatis Plus的电影购票系统后端项目，实现了用户管理、电影管理、订单管理、权限控制等核心功能。

## 技术栈

- **Spring Boot 3.4.6** - 主框架
- **MyBatis Plus 3.5.12** - ORM框架
- **MySQL 8.0** - 数据库
- **Spring Security** - 安全框架
- **JWT** - 身份认证
- **Swagger** - API文档
- **Lombok** - 代码简化

## 功能特性

### 🔐 权限管理
- **三层权限体系**：
  - 普通用户 (USER) - 浏览、搜索、购票
  - 管理员 (ADMIN) - 用户管理、订单管理、电影管理
  - 系统管理员 (SUPER_ADMIN) - 所有权限 + 角色管理

### 👥 用户管理
- 用户注册/登录
- JWT令牌认证
- 用户信息管理
- 角色分配

### 🎬 电影管理
- 电影信息CRUD
- 电影搜索
- 电影状态管理

### 🎫 订单管理
- 订单创建
- 订单状态管理
- 支付集成
- 退款处理

### 🏢 影厅管理
- 影厅信息管理
- 座位管理
- 场次安排

## 快速开始

### 1. 环境要求
- JDK 21+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
```sql
-- 执行数据库脚本
source db.sql
```

### 3. 配置文件
修改 `application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/movie_booking_system
    username: your_username
    password: your_password
```

### 4. 启动项目
```bash
mvn spring-boot:run
```

### 5. 访问地址
- 应用地址：http://localhost:8888
- API文档：http://localhost:8888/swagger-ui/

## 默认账户

### 系统管理员
- 用户名：`superadmin`
- 密码：`admin123`
- 权限：所有权限

### 管理员
- 用户名：`admin`
- 密码：`admin123`
- 权限：管理权限

### 普通用户
- 用户名：`testuser`
- 密码：`123456`
- 权限：基本权限

## API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出

### 用户管理
- `GET /api/users` - 获取用户列表（管理员）
- `GET /api/users/{id}` - 获取用户详情（管理员）
- `POST /api/users` - 创建用户（管理员）
- `PUT /api/users/{id}` - 更新用户（管理员）
- `DELETE /api/users/{id}` - 删除用户（系统管理员）
- `GET /api/users/current` - 获取当前用户信息

### 角色管理
- `GET /api/roles` - 获取所有角色（系统管理员）
- `PUT /api/roles/users/{userId}/promote` - 提升为管理员（系统管理员）
- `PUT /api/roles/users/{userId}/demote` - 降级为普通用户（系统管理员）

## 权限说明

### 普通用户权限
- 浏览电影信息
- 查看场次信息
- 创建订单
- 支付处理

### 管理员权限
- 用户管理（查看、创建、更新、状态管理）
- 电影管理（完整CRUD）
- 订单管理（查看、更新、退款）
- 场次管理（完整CRUD）
- 影厅管理（完整CRUD）
- 支付管理（查看、处理、退款）

### 系统管理员权限
- 所有管理员权限
- 用户删除
- 角色管理
- 系统配置
- 日志查看
- 数据备份

## 项目结构

```
src/main/java/com/example/movie_booking_backend/
├── common/                 # 公共组件
│   ├── annotation/        # 自定义注解
│   ├── config/           # 配置类
│   ├── constants/        # 常量定义
│   ├── exception/        # 异常处理
│   ├── interceptor/      # 拦截器
│   └── utils/            # 工具类
├── mapper/               # MyBatis映射器
├── model/                # 数据模型
│   ├── domain/          # 实体类
│   ├── dto/             # 数据传输对象
│   └── vo/              # 视图对象
├── service/              # 服务层
│   └── impl/            # 服务实现
└── web/                  # 控制器层
    └── controller/      # 控制器
```

## 开发说明

### 添加新接口
1. 在对应的Controller中添加方法
2. 使用 `@RequireRole` 注解设置权限
3. 添加Swagger注解
4. 实现业务逻辑

### 权限控制
使用 `@RequireRole` 注解控制接口权限：
```java
@RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
public JsonResponse<String> adminOnlyMethod() {
    // 只有管理员和系统管理员可以访问
}
```

### 异常处理
系统提供全局异常处理，包括：
- 参数验证异常
- 业务逻辑异常
- 权限不足异常
- 系统异常

## 部署说明

### 打包
```bash
mvn clean package
```

### 运行
```bash
java -jar target/mybatis-plus-demo-0.0.1-SNAPSHOT.jar
```

## 注意事项

1. 首次运行需要执行数据库脚本
2. 确保MySQL服务正常运行
3. 修改配置文件中的数据库连接信息
4. 生产环境请修改JWT密钥

## 联系方式

如有问题，请联系开发团队。 