package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie_booking_backend.model.domain.UserGroupRelations;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户分组关系表 Mapper 接口
 * </p>
 *
 * @author tjl
 * @since 2025-06-26
 */
public interface UserGroupRelationsMapper extends BaseMapper<UserGroupRelations> {
    /**
     * 检查用户与分组的关联关系是否存在
     */
    Integer existsByUserIdAndGroupIdAndIsDeletedFalse(@Param("userId") Long userId, @Param("groupId") Long groupId);

    /**
     * 查询用户与分组的关联关系（包括已逻辑删除的）
     */
    UserGroupRelations selectByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    /**
     * 统计分组中的用户数量
     */
    Integer countByGroupIdAndIsDeletedFalse(@Param("groupId") Long groupId);

    /**
     * 统计用户所属的分组数量
     */
    Integer countByUserIdAndIsDeletedFalse(@Param("userId") Long userId);

    /**
     * 删除用户与分组的关联关系（逻辑删除）
     */
    int deleteByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    /**
     * 删除分组的所有关联关系（逻辑删除）
     */
    int deleteByGroupId(@Param("groupId") Long groupId);

    /**
     * 删除用户的所有关联关系（逻辑删除）
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 恢复用户与分组的关联关系（将逻辑删除标记设为false）
     */
    int restoreByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
