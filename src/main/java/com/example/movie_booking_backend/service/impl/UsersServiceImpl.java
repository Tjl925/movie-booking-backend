package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.RolesMapper;
import com.example.movie_booking_backend.model.domain.Roles;
import com.example.movie_booking_backend.model.domain.Users;
import com.example.movie_booking_backend.mapper.UsersMapper;
import com.example.movie_booking_backend.model.dto.UserCreationDTO;
import com.example.movie_booking_backend.model.dto.UserUpdateDTO;
import com.example.movie_booking_backend.model.vo.UserVO;
import com.example.movie_booking_backend.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
// 移除PasswordEncoder导入
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private RolesMapper rolesMapper;

    // 移除PasswordEncoder注入

    @Override
    public Page<UserVO> listUsers(Page<Users> page, String username, String email, String status) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);

        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (email != null && !email.isEmpty()) {
            queryWrapper.like("email", email);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("created_at");

        Page<Users> usersPage = this.page(page, queryWrapper);

        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(usersPage, voPage);

        List<UserVO> userVOList = usersPage.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    Roles role = rolesMapper.selectById(user.getRoleId());
                    if (role != null) {
                        vo.setRoleName(role.getDisplayName());
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(userVOList);
        return voPage;
    }

    @Override
    @Transactional
    public Users createUser(UserCreationDTO userCreationDTO) {
        // 检查用户名是否已存在
        if (this.getOne(new QueryWrapper<Users>().eq("username", userCreationDTO.getUsername()).eq("is_deleted", false)) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查角色是否存在
        Roles role = rolesMapper.selectById(userCreationDTO.getRoleId());
        if (role == null || role.getDeleted()) {
            throw new BusinessException("指定的角色不存在");
        }

        Users user = new Users();
        BeanUtils.copyProperties(userCreationDTO, user);

        user.setPassword(userCreationDTO.getPassword()); // 使用明文密码
        user.setStatus("ACTIVE");
        user.setLoginCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);

        this.save(user);
        return user;
    }

    @Override
    @Transactional
    public Users updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        Users existingUser = this.getById(id);
        if (existingUser == null || existingUser.getDeleted()) {
            throw new BusinessException("用户不存在");
        }

        // 如果要更新角色，检查角色是否存在
        if (userUpdateDTO.getRoleId() != null) {
            Roles role = rolesMapper.selectById(userUpdateDTO.getRoleId());
            if (role == null || role.getDeleted()) {
                throw new BusinessException("指定的角色不存在");
            }
        }

        BeanUtils.copyProperties(userUpdateDTO, existingUser);
        existingUser.setId(id);
        existingUser.setUpdatedAt(LocalDateTime.now());

        this.updateById(existingUser);
        return existingUser;
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        Users user = this.getById(id);
        if (user == null || user.getDeleted()) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(newPassword); // 使用明文密码
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);
    }
}
