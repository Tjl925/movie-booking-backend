-- 修复用户密码脚本
-- 这个脚本将更新所有初始用户的密码为明文密码

USE movie_booking_system;

-- 更新系统管理员密码为：admin123
UPDATE users 
SET password = 'admin123'
WHERE username = 'superadmin';

-- 更新管理员密码为：admin123  
UPDATE users 
SET password = 'admin123'
WHERE username = 'admin';

-- 更新测试用户密码为：123456
UPDATE users 
SET password = '123456'
WHERE username = 'testuser';

-- 更新员工用户密码为：123456
UPDATE users 
SET password = '123456'
WHERE username = 'staff';

-- 验证更新结果
SELECT username, password FROM users WHERE username IN ('superadmin', 'admin', 'testuser', 'staff');