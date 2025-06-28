package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie_booking_backend.model.domain.MovieSessions;
import com.example.movie_booking_backend.model.vo.SeatVO;
import com.example.movie_booking_backend.model.vo.SessionInfo;
import com.example.movie_booking_backend.model.vo.SessionVO;

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

    List<SessionInfo> findSessionInfoByMovieId(Long movieId);
}
