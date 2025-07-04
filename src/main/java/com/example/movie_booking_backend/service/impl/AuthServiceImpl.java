package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.common.utils.JwtUtils;
import com.example.movie_booking_backend.mapper.RolesMapper;
import com.example.movie_booking_backend.mapper.UsersMapper;
import com.example.movie_booking_backend.model.domain.Roles;
import com.example.movie_booking_backend.model.domain.Users;
import com.example.movie_booking_backend.model.dto.LoginDTO;
import com.example.movie_booking_backend.model.dto.RegisterDTO;
import com.example.movie_booking_backend.model.vo.LoginResponseVO;
import com.example.movie_booking_backend.model.vo.UserVO;
import com.example.movie_booking_backend.service.IAuthService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private RolesMapper rolesMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginResponseVO login(LoginDTO loginDTO) {
        // 查询用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername())
                   .eq("is_deleted", false);
        Users user = usersMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码（明文比较）
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("用户已被禁用");
        }

        // 查询角色信息
        Roles role = rolesMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new BusinessException("用户角色不存在");
        }

        // 更新登录信息
        user.setLastLogin(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        usersMapper.updateById(user);

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), role.getName());

        // 构建响应
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setExpiresIn(86400L); // 24小时

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoleName(role.getName());
        response.setUserInfo(userVO);

        return response;
    }

    @Override
    @Transactional
    public LoginResponseVO register(RegisterDTO registerDTO) {
        // 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDTO.getUsername())
                   .eq("is_deleted", false);
        if (usersMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", registerDTO.getEmail())
                       .eq("is_deleted", false);
            if (usersMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 检查手机号是否已存在
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", registerDTO.getPhone())
                       .eq("is_deleted", false);
            if (usersMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 获取普通用户角色ID
        QueryWrapper<Roles> roleQuery = new QueryWrapper<>();
        roleQuery.eq("name", "USER");
        Roles userRole = rolesMapper.selectOne(roleQuery);
        if (userRole == null) {
            throw new BusinessException("系统错误：用户角色不存在");
        }
        if (StringUtils.isNotBlank(registerDTO.getOpenId())) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("open_id", registerDTO.getOpenId())
                    .eq("is_deleted", false);
            if (usersMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("该QQ账号已绑定其他用户");
            }
        }
        // 创建用户
        Users user = new Users();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword()); // 使用明文密码
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRoleId(userRole.getId());
        user.setStatus("ACTIVE");
        if (StringUtils.isNotBlank(registerDTO.getOpenId())) {
            user.setOpenId(registerDTO.getOpenId());
        }
        user.setLoginCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);
        usersMapper.insert(user);
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRoleId().toString());

        // 构建响应
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setExpiresIn(86400L); // 24小时

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoleName(user.getRoleId().toString());
        response.setUserInfo(userVO);

        return response;


    }

    @Override
    public void logout(String token) {
        System.out.println("退出成功");
    }

    @Override
    public String validate(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return "两次输入的密码不一致";
        }

        // 检查用户名是否已存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerDTO.getUsername())
                .eq("is_deleted", false);
        if (usersMapper.selectCount(queryWrapper) > 0) {
            return "用户名已存在";
        }

        // 检查邮箱是否已存在
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", registerDTO.getEmail())
                    .eq("is_deleted", false);
            if (usersMapper.selectCount(queryWrapper) > 0) {
                return "邮箱已被注册";
            }
        }

        // 检查手机号是否已存在
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", registerDTO.getPhone())
                    .eq("is_deleted", false);
            if (usersMapper.selectCount(queryWrapper) > 0) {
                return "手机号已被注册";
            }
        }

        // 获取普通用户角色ID
        QueryWrapper<Roles> roleQuery = new QueryWrapper<>();
        roleQuery.eq("name", "USER");
        Roles userRole = rolesMapper.selectOne(roleQuery);
        if (userRole == null) {
            throw new BusinessException("系统错误：用户角色不存在");
        }
        if (StringUtils.isNotBlank(registerDTO.getOpenId())) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("open_id", registerDTO.getOpenId())
                    .eq("is_deleted", false);
            if (usersMapper.selectCount(queryWrapper) > 0) {
                return "该QQ账号已绑定其他用户";
            }
        }
        return "true";
    }
}