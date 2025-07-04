package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.Users;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface UsersMapper extends BaseMapper<Users> {
    /**
     * 根据ID查找未删除的用户
     */
    Users selectByIdAndIsDeletedFalse(@Param("id") Long id);

    /**
     * 根据分组ID查询用户列表（分页）
     */
    Page<Users> selectUsersByGroupId(Page<Users> page, @Param("groupId") Long groupId);
}
