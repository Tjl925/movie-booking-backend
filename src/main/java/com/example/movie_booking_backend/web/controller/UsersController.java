package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.model.domain.Users;
import com.example.movie_booking_backend.model.dto.*;
import com.example.movie_booking_backend.model.vo.UserVO;
import com.example.movie_booking_backend.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 前端控制器
 *
 * @author tjl
 * @version v1.0
 * @since 2025-06-23
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private IUsersService usersService;

    @ApiOperation("获取用户列表")
    @GetMapping
    public JsonResponse<Page<UserVO>> getUserList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("用户名") @RequestParam(required = false) String username,
            @ApiParam("邮箱") @RequestParam(required = false) String email,
            @ApiParam("状态") @RequestParam(required = false) String status) {

        Page<Users> page = new Page<>(current, size);
        Page<UserVO> voPage = usersService.listUsers(page, username, email, status);
        return JsonResponse.success(voPage);
    }

    @ApiOperation("根据ID获取用户")
    @GetMapping("/{id}")
    public JsonResponse<UserVO> getUserById(@PathVariable Long id) {
        Users user = usersService.getById(id);
        if (user == null || user.getDeleted()) {
            return JsonResponse.failure("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        // 这里也可以补充角色信息
        return JsonResponse.success(vo);
    }

    @ApiOperation("更新用户")
    @PutMapping("/{id}")
    public JsonResponse<UserVO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        Users user = usersService.updateUser(id, userUpdateDTO);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return JsonResponse.success(userVO, "用户更新成功");
    }

    @ApiOperation("更新用户个人信息")
    @PostMapping("/profile/{id}")
    public JsonResponse<UserVO> updateUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateDTO updateDTO) {
        try {
            Users user = usersService.updateUserProfile(id, updateDTO);
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return JsonResponse.success(vo, "个人信息更新成功");
        } catch (BusinessException e) {
            return JsonResponse.failure(e.getMessage());
        } catch (IllegalArgumentException e) {
            return JsonResponse.failure(e.getMessage());
        } catch (Exception e) {
            return JsonResponse.failure("系统错误，请稍后再试");
        }
    }


    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public JsonResponse<String> deleteUser(@PathVariable Long id) {
        Users user = usersService.getById(id);
        if (user == null || user.getDeleted()) {
            return JsonResponse.failure("用户不存在");
        }

        // 软删除
        user.setDeleted(true);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        boolean success = usersService.updateById(user);
        if (success) {
            return JsonResponse.successMessage("用户删除成功");
        } else {
            return JsonResponse.failure("用户删除失败");
        }
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public JsonResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Users user = usersService.getById(userId);
        if (user == null || user.getDeleted()) {
            return JsonResponse.failure("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return JsonResponse.success(vo);
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/{id}/status")
    public JsonResponse<String> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Users user = usersService.getById(id);
        if (user == null || user.getDeleted()) {
            return JsonResponse.failure("用户不存在");
        }

        user.setStatus(status);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        boolean success = usersService.updateById(user);
        if (success) {
            return JsonResponse.successMessage("用户状态更新成功");
        } else {
            return JsonResponse.failure("用户状态更新失败");
        }
    }

    @ApiOperation("重置用户密码")
    @PutMapping("/{id}/reset-password")
    public JsonResponse<String> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        usersService.resetPassword(id, resetPasswordDTO.getNewPassword());
        return JsonResponse.successMessage("密码重置成功");
    }

    @ApiOperation("上传用户头像")
    @PostMapping("/{id}/avatar")
    public JsonResponse<String> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            // 调用userService处理头像上传
            String avatarUrl = usersService.uploadAvatar(id, file);
            return JsonResponse.success(avatarUrl, "头像上传成功");
        } catch (Exception e) {
            return JsonResponse.failure("头像上传失败: " + e.getMessage());
        }
    }

    @ApiOperation("修改用户密码")
    @PostMapping("/change-password")
    public JsonResponse<String> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            usersService.changePassword(changePasswordDTO);
            return JsonResponse.successMessage("密码修改成功");
        } catch (Exception e) {
            return JsonResponse.failure("密码修改失败: " + e.getMessage());
        }
    }
}

