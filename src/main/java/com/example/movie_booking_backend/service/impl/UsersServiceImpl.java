package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.common.utils.JwtUtils;
import com.example.movie_booking_backend.mapper.RolesMapper;
import com.example.movie_booking_backend.model.domain.Roles;
import com.example.movie_booking_backend.model.domain.Users;
import com.example.movie_booking_backend.mapper.UsersMapper;
import com.example.movie_booking_backend.model.dto.*;
import com.example.movie_booking_backend.model.vo.AuthResultVO;
import com.example.movie_booking_backend.model.vo.QQBindVO;
import com.example.movie_booking_backend.model.vo.UserVO;
import com.example.movie_booking_backend.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
// 移除PasswordEncoder导入
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
    @Autowired
    private JwtUtils jwtUtils;

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

        Page<Users> usersPage = this.page(page, queryWrapper);

        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(usersPage, voPage);

        List<UserVO> userVOList = usersPage.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    Roles role = rolesMapper.selectById(user.getRoleId());
                    if (role != null) {
                        vo.setRoleName(role.getName());
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

    @Override
    @Transactional
    public Users updateUserProfile(Long id, UserProfileUpdateDTO updateDTO) {
        // 参数校验
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (updateDTO == null) {
            throw new IllegalArgumentException("更新数据不能为空");
        }

        Users user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证并更新各个字段
        if (updateDTO.getUsername() != null) {
            validateUsername(updateDTO.getUsername(), id); // 传入当前用户ID
            user.setUsername(updateDTO.getUsername());
        }
        if (updateDTO.getEmail() != null) {
            validateEmail(updateDTO.getEmail(), id); // 传入当前用户ID
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null) {
            validatePhone(updateDTO.getPhone(), id); // 传入当前用户ID
            user.setPhone(updateDTO.getPhone());
        }

        updateById(user);
        return user;
    }

    private void validateUsername(String username, Long currentUserId) {
        // 基础校验
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度必须在3-20个字符之间");
        }
        if (!username.matches("^[a-zA-Z0-9_\\-]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字、下划线和连字符");
        }
        if (username.startsWith("_") || username.startsWith("-")) {
            throw new IllegalArgumentException("用户名不能以下划线或连字符开头");
        }

        // 唯一性校验（排除当前用户）
        long count = count(new QueryWrapper<Users>()
                .eq("username", username)
                .ne("id", currentUserId) // 排除当前用户
                .eq("is_deleted", false));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已被使用");
        }
    }

    private void validateEmail(String email, Long currentUserId) {
        // 基础校验
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!email.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        // 唯一性校验（排除当前用户）
        long count = count(new QueryWrapper<Users>()
                .eq("email", email)
                .ne("id", currentUserId) // 排除当前用户
                .eq("is_deleted", false));
        if (count > 0) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
    }

    private void validatePhone(String phone, Long currentUserId) {
        // 基础校验
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        // 唯一性校验（排除当前用户）
        long count = count(new QueryWrapper<Users>()
                .eq("phone", phone)
                .ne("id", currentUserId) // 排除当前用户
                .eq("is_deleted", false));
        if (count > 0) {
            throw new IllegalArgumentException("手机号已被使用");
        }
    }

    @Value("${file-upload-path}")
    private String uploadBaseDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif"
    );

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 验证用户存在性
        Users user = this.getById(userId);
        if (user == null || user.getDeleted()) {
            throw new BusinessException("用户不存在或已被删除");
        }

        // 2. 验证文件有效性
        validateFile(file);

        // 3. 生成存储路径
        String relativePath = generateAvatarPath(userId, file.getOriginalFilename());
        Path storagePath = Paths.get(uploadBaseDir, relativePath);

        try {
            // 4. 确保目录存在
            Files.createDirectories(storagePath.getParent());

            // 5. 存储文件（替换已存在的）
            file.transferTo(storagePath);

            // 6. 更新数据库
            user.setAvatar(relativePath); // 存储相对路径如：/avatar/user_123.jpg
            this.updateById(user);

            return relativePath;
        } catch (IOException e) {
            log.error("头像存储失败", e);
            throw new BusinessException("头像上传失败，请重试");
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        Users user = this.getById(changePasswordDTO.getUserId());
        if (user == null || user.getDeleted()) {
            throw new BusinessException("用户不存在");
        }

        // 检查旧密码是否正确
        if (!user.getPassword().equals(changePasswordDTO.getOldPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 检查新密码和确认密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        // 验证新密码是否符合规范（例如长度）
        validatePassword(changePasswordDTO.getNewPassword());

        // 更新密码
        user.setPassword(changePasswordDTO.getNewPassword());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        this.updateById(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("密码长度必须在6-20个字符之间");
        }
    }

    private void validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("头像文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持JPEG/PNG/GIF格式的图片");
        }

        // 检查文件大小（示例限制2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("头像大小不能超过2MB");
        }
    }

    private String generateAvatarPath(Long userId, String originalFilename) {
        // 获取文件扩展名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成唯一文件名（防止冲突）
        String filename = "user_" + userId + "_" + System.currentTimeMillis() + extension;

        // 返回相对路径（如：/avatar/user_123_1681234567890.jpg）
        return "/avatar/" + filename;
    }

    @Override
    public AuthResultVO checkQQBind(String openId, String nickname, String avatar) {
        // 1. 检查是否已绑定
        Users user = lambdaQuery().eq(Users::getOpenId, openId).one();

        if (user != null) {
            // 2. 已绑定 - 直接生成token
            String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRoleId().toString());
            return AuthResultVO.bound(user, token);
        }

        // 3. 未绑定 - 生成建议用户名
        String suggestedUsername = generateUsername(nickname);

        return AuthResultVO.unbound(QQBindVO.builder()
                .openId(openId)
                .nickname(nickname)
                .avatar(avatar)
                .suggestedUsername(suggestedUsername)
                .build());
    }

    private String generateUsername(String nickname) {
        String cleanName = nickname.replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "");
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4); // 取前4位
        return cleanName + "_" + randomSuffix.toLowerCase();
    }

    @Override
    @Transactional
    public AuthResultVO bindQQAccount(BindRequestDTO request) {
        // 1. 验证必填字段
        if (StringUtils.isBlank(request.getUsername()) ){
            throw new BusinessException("用户名不能为空");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (StringUtils.isBlank(request.getOpenId())) {
            throw new BusinessException("QQ openId不能为空");
        }

        // 2. 查询用户
        Users user = getOne(new QueryWrapper<Users>()
                .eq("username", request.getUsername())
                .eq("is_deleted", false));

        // 3. 验证用户是否存在
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 4. 验证密码是否正确
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 5. 检查是否已绑定其他QQ
        if (StringUtils.isNotBlank(user.getOpenId())) {
            if (user.getOpenId().equals(request.getOpenId())) {
                throw new BusinessException("该QQ已绑定当前账号");
            } else {
                throw new BusinessException("该账号已绑定其他QQ");
            }
        }

        // 6. 执行绑定
        user.setLastLogin(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        user.setOpenId(request.getOpenId());
        updateById(user);

        // 7. 返回绑定成功信息
        return AuthResultVO.bound(user,
                jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRoleId().toString()));
    }

}
