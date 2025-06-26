package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.UserGroupRelationsMapper;
import com.example.movie_booking_backend.mapper.UsersMapper;
import com.example.movie_booking_backend.model.domain.UserGroupRelations;
import com.example.movie_booking_backend.model.domain.UserGroups;
import com.example.movie_booking_backend.model.domain.Users;
import com.example.movie_booking_backend.mapper.UserGroupsMapper;
import com.example.movie_booking_backend.model.dto.UserGroupDTO;
import com.example.movie_booking_backend.model.vo.UserVO;
import com.example.movie_booking_backend.service.IUserGroupsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户分组表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-26
 */
@Service
public class UserGroupsServiceImpl extends ServiceImpl<UserGroupsMapper, UserGroups> implements IUserGroupsService {
    @Autowired
    private UserGroupsMapper userGroupsMapper;

    @Autowired
    private UserGroupRelationsMapper userGroupRelationsMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public List<UserGroups> getAllUserGroups() {
        List<UserGroups> groups = userGroupsMapper.selectAllByIsDeletedFalse();
        
        // 为每个分组添加用户数量信息
        for (UserGroups group : groups) {
            Integer userCount = userGroupRelationsMapper.countByGroupIdAndIsDeletedFalse(group.getId());
            group.setUserCount(userCount);
        }
        
        return groups;
    }

    @Override
    @Transactional
    public UserGroups createUserGroup(UserGroupDTO userGroupDTO) {
        UserGroups group = new UserGroups();
        BeanUtils.copyProperties(userGroupDTO, group);
        group.setDeleted(false);
        this.save(group);
        return group;
    }

    @Override
    @Transactional
    public UserGroups updateUserGroup(UserGroupDTO userGroupDTO) {
        UserGroups group = userGroupsMapper.selectByIdAndIsDeletedFalse(userGroupDTO.getId());
        if (group == null) {
            throw new BusinessException("用户分组不存在");
        }

        // 系统分组不允许修改类型
        if ("SYSTEM".equals(group.getType())) {
            userGroupDTO.setType("SYSTEM");
        }

        BeanUtils.copyProperties(userGroupDTO, group, "id", "createdAt", "updatedAt", "deleted");
        this.updateById(group);
        return group;
    }

    @Override
    @Transactional
    public void deleteUserGroup(Long id) {
        UserGroups group = userGroupsMapper.selectByIdAndIsDeletedFalse(id);
        if (group == null) {
            throw new BusinessException("用户分组不存在");
        }

        // 系统分组不允许删除
        if ("SYSTEM".equals(group.getType())) {
            throw new IllegalArgumentException("系统分组不允许删除");
        }

        group.setDeleted(true);
        this.updateById(group);

        // 删除关联关系
        userGroupRelationsMapper.deleteByGroupId(id);
    }

    @Override
    public Page<UserVO> getUsersInGroup(Long groupId, Integer page, Integer size) {
        // 检查分组是否存在
        UserGroups group = userGroupsMapper.selectByIdAndIsDeletedFalse(groupId);
        if (group == null) {
            throw new BusinessException("用户分组不存在");
        }

        // 查询分页数据
        Page<Users> userPage = new Page<>(page, size);
        Page<Users> usersPage = usersMapper.selectUsersByGroupId(userPage, groupId);

        // 转换为VO对象
        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(usersPage, voPage);

        List<UserVO> userVOList = usersPage.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    // 如果需要，可以在这里设置额外的属性
                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(userVOList);
        return voPage;
    }

    @Override
    @Transactional
    public void addUserToGroup(Long groupId, Long userId) {
        // 检查分组是否存在
        UserGroups group = userGroupsMapper.selectByIdAndIsDeletedFalse(groupId);
        if (group == null) {
            throw new BusinessException("用户分组不存在");
        }

        // 检查用户是否存在
        Users user = usersMapper.selectByIdAndIsDeletedFalse(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查关联关系是否已存在（未被逻辑删除的）
        if (userGroupRelationsMapper.existsByUserIdAndGroupIdAndIsDeletedFalse(userId, groupId) > 0) {
            return; // 已存在且未被逻辑删除则不做处理
        }

        // 查询是否存在被逻辑删除的关联关系
        UserGroupRelations existingRelation = userGroupRelationsMapper.selectByUserIdAndGroupId(userId, groupId);
        if (existingRelation != null && existingRelation.getDeleted()) {
            // 存在被逻辑删除的关系，恢复它
            userGroupRelationsMapper.restoreByUserIdAndGroupId(userId, groupId);
        } else {
            // 不存在任何关系，创建新的关联关系
            UserGroupRelations relation = new UserGroupRelations();
            relation.setUserId(userId);
            relation.setGroupId(groupId);
            relation.setDeleted(false);
            userGroupRelationsMapper.insert(relation);
        }
    }

    @Override
    @Transactional
    public void removeUserFromGroup(Long groupId, Long userId) {
        // 检查分组是否存在
        UserGroups group = userGroupsMapper.selectByIdAndIsDeletedFalse(groupId);
        if (group == null) {
            throw new BusinessException("用户分组不存在");
        }

        // 检查用户是否存在
        Users user = usersMapper.selectByIdAndIsDeletedFalse(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除关联关系
        userGroupRelationsMapper.deleteByUserIdAndGroupId(userId, groupId);
    }

    @Override
    public List<UserGroups> getGroupsByUser(Long userId) {
        // 检查用户是否存在
        Users user = usersMapper.selectByIdAndIsDeletedFalse(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<UserGroups> groups = userGroupsMapper.selectGroupsByUserId(userId);
        
        // 为每个分组添加用户数量信息
        for (UserGroups group : groups) {
            Integer userCount = userGroupRelationsMapper.countByGroupIdAndIsDeletedFalse(group.getId());
            group.setUserCount(userCount);
        }
        
        return groups;
    }
}
