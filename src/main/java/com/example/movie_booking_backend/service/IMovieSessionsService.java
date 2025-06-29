package com.example.movie_booking_backend.service;

import com.example.movie_booking_backend.model.domain.MovieSessions;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.SeatSelectionDTO;
import com.example.movie_booking_backend.model.dto.SeatStatusUpdateDTO;
import com.example.movie_booking_backend.model.dto.SessionDTO;
import com.example.movie_booking_backend.model.vo.SeatVO;
import com.example.movie_booking_backend.model.vo.SessionInfoVO;
import com.example.movie_booking_backend.model.vo.SeatsSessionsVO;
import com.example.movie_booking_backend.model.vo.SessionVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 电影场次表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
public interface IMovieSessionsService extends IService<MovieSessions> {

    SessionVO createSession(SessionDTO sessionDTO);

    SessionVO updateSession(Long sessionId, SessionDTO sessionDTO);

    void deleteSession(Long sessionId);

    List<SessionVO> getSessionsByMovieAndDate(Long movieId, LocalDate date);

    SessionVO getSessionDetails(Long sessionId);

    List<SeatVO> getSeatStatusForSession(Long sessionId);

    List<SessionInfoVO> getSessionInfosByMovieId(Long movieId);

    SeatsSessionsVO getSeatsForSelection(Long id);


}
