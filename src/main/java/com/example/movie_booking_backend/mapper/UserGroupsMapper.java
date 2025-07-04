package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie_booking_backend.model.domain.UserGroups;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户分组表 Mapper 接口
 * </p>
 *
 * @author tjl
 * @since 2025-06-26
 */
public interface UserGroupsMapper extends BaseMapper<UserGroups> {
    /**
     * 查找所有未删除的用户分组
     */
    List<UserGroups> selectAllByIsDeletedFalse();

    /**
     * 根据ID查找未删除的用户分组
     */
    UserGroups selectByIdAndIsDeletedFalse(@Param("id") Long id);

    /**
     * 根据用户ID查找该用户所属的所有分组
     */
    List<UserGroups> selectGroupsByUserId(@Param("userId") Long userId);
}
