package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.dto.UserGroupDTO;
import com.example.movie_booking_backend.model.vo.UserVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.service.IUserGroupsService;
import com.example.movie_booking_backend.model.domain.UserGroups;

import java.util.List;


/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-26
 * @version v1.0
 */
@RestController
@RequestMapping("/api")
public class UserGroupsController {

    private final Logger logger = LoggerFactory.getLogger( UserGroupsController.class );

    @Autowired
    private IUserGroupsService userGroupsService;

    /**
     * 获取所有用户分组
     */
    @GetMapping("/user-groups")
    public JsonResponse<List<UserGroups>> getAllUserGroups() throws Exception {
        List<UserGroups> groups = userGroupsService.getAllUserGroups();
        return JsonResponse.success(groups);
    }

    /**
     * 创建用户分组
     */
    @PostMapping("/user-groups")
    public JsonResponse<UserGroups> createUserGroup(@RequestBody UserGroupDTO userGroupDTO) throws Exception {
        UserGroups createdGroup = userGroupsService.createUserGroup(userGroupDTO);
        return JsonResponse.success(createdGroup);
    }

    /**
     * 更新用户分组
     */
    @PutMapping("/user-groups/{id}")
    public JsonResponse<UserGroups> updateUserGroup(
            @PathVariable Long id,
            @RequestBody UserGroupDTO userGroupDTO) throws Exception{
        userGroupDTO.setId(id);
        UserGroups updatedGroup = userGroupsService.updateUserGroup(userGroupDTO);
        return JsonResponse.success(updatedGroup);
    }

    /**
     * 删除用户分组
     */
    @DeleteMapping("/user-groups/{id}")
    public JsonResponse<String> deleteUserGroup(@PathVariable Long id) throws Exception{
        userGroupsService.deleteUserGroup(id);
        return JsonResponse.successMessage("删除分组成功");
    }

    /**
     * 获取分组中的用户列表
     */
    @GetMapping("/user-groups/{groupId}/users")
    public JsonResponse<Page<UserVO>> getUsersInGroup(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) throws Exception{
        Page<UserVO> users = userGroupsService.getUsersInGroup(groupId, page, size);
        return JsonResponse.success(users);
    }

    /**
     * 将用户添加到分组
     */
    @PostMapping("/user-groups/{groupId}/users/{userId}")
    public JsonResponse<String> addUserToGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) throws Exception{
        userGroupsService.addUserToGroup(groupId, userId);
        return JsonResponse.successMessage("添加成功");
    }

    /**
     * 将用户从分组中移除
     */
    @DeleteMapping("/user-groups/{groupId}/users/{userId}")
    public JsonResponse<String> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) throws Exception{
        userGroupsService.removeUserFromGroup(groupId, userId);
        return JsonResponse.successMessage("移出成功");
    }

    /**
     * 获取用户所属的分组列表
     */
    @GetMapping("/users/{userId}/groups")
    public JsonResponse<List<UserGroups>> getGroupsByUser(@PathVariable Long userId) throws Exception{
        List<UserGroups> groups = userGroupsService.getGroupsByUser(userId);
        return JsonResponse.success(groups);
    }

}

