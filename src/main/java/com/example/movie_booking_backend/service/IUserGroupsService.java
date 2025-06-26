package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.UserGroups;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.UserGroupDTO;
import com.example.movie_booking_backend.model.vo.UserVO;

import java.util.List;

/**
 * <p>
 * 用户分组表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-26
 */
public interface IUserGroupsService extends IService<UserGroups> {
    /**
     * 获取所有用户分组
     */
    List<UserGroups> getAllUserGroups();

    /**
     * 创建用户分组
     */
    UserGroups createUserGroup(UserGroupDTO userGroupDTO);

    /**
     * 更新用户分组
     */
    UserGroups updateUserGroup(UserGroupDTO userGroupDTO);

    /**
     * 删除用户分组
     */
    void deleteUserGroup(Long id);

    /**
     * 获取分组中的用户列表
     */
    Page<UserVO> getUsersInGroup(Long groupId, Integer page, Integer size);

    /**
     * 将用户添加到分组
     */
    void addUserToGroup(Long groupId, Long userId);

    /**
     * 将用户从分组中移除
     */
    void removeUserFromGroup(Long groupId, Long userId);

    /**
     * 获取用户所属的分组列表
     */
    List<UserGroups> getGroupsByUser(Long userId);
}
