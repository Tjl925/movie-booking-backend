package com.example.movie_booking_backend.service;

import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.model.domain.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.dto.*;
import com.example.movie_booking_backend.model.vo.AuthResultVO;
import com.example.movie_booking_backend.model.vo.UserVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 * @version v1.0
 */
public interface IUsersService extends IService<Users> {

    Page<UserVO> listUsers(Page<Users> page, String username, String email, String status);

    Users createUser(UserCreationDTO userCreationDTO);

    Users updateUser(Long id, UserUpdateDTO userUpdateDTO);

    void resetPassword(Long id, String newPassword);

    @Transactional
    Users updateUserProfile(Long id, UserProfileUpdateDTO updateDTO);

    String uploadAvatar(Long id, MultipartFile file);
    void changePassword(ChangePasswordDTO changePasswordDTO);

    AuthResultVO checkQQBind(String openId, String nickname, String avatar);
    AuthResultVO bindQQAccount(BindRequestDTO request);
    String getPasswordByEmail(String email) throws BusinessException;
}
