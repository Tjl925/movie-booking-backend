package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie_booking_backend.model.domain.MovieSessions;
import com.example.movie_booking_backend.model.vo.SessionInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 电影场次表 Mapper 接口
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
public interface MovieSessionsMapper extends BaseMapper<MovieSessions> {

    List<SessionInfoVO> findSessionInfoByMovieId(Long movieId);
    
    /**
     * 根据关键字查询场次ID列表（匹配电影标题或影厅名称）
     * @param keyword 关键字
     * @return 场次ID列表
     */
    List<Long> findSessionIdsByKeyword(@Param("keyword") String keyword);
}
