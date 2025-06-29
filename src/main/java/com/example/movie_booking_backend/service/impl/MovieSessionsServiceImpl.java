package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.*;
import com.example.movie_booking_backend.model.domain.*;
import com.example.movie_booking_backend.model.dto.SeatSelectionDTO;
import com.example.movie_booking_backend.model.dto.SeatStatusUpdateDTO;
import com.example.movie_booking_backend.model.dto.SessionDTO;
import com.example.movie_booking_backend.model.vo.*;
import com.example.movie_booking_backend.service.IHallsService;
import com.example.movie_booking_backend.service.IMovieSessionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.movie_booking_backend.service.ISeatsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 电影场次表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Service
public class MovieSessionsServiceImpl extends ServiceImpl<MovieSessionsMapper, MovieSessions> implements IMovieSessionsService {

    @Autowired
    private MoviesMapper moviesMapper;

    @Autowired
    private HallsMapper hallsMapper;

    @Autowired
    private ISeatsService seatsService;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private MovieSessionsMapper movieSessionsMapper;
    @Autowired
    private SeatsSessionsMapper seatsSessionsMapper;
    @Override
    @Transactional
    public SessionVO createSession(SessionDTO sessionDTO) {
        Movies movie = moviesMapper.selectById(sessionDTO.getMovieId());
        if (movie == null || movie.getDeleted()) throw new BusinessException("电影不存在");

        Halls hall = hallsMapper.selectById(sessionDTO.getHallId());
        if (hall == null || hall.getDeleted()) throw new BusinessException("影厅不存在");

        LocalDateTime startTime = sessionDTO.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        checkTimeConflict(sessionDTO.getHallId(), startTime, endTime, null);

        MovieSessions session = new MovieSessions();
        BeanUtils.copyProperties(sessionDTO, session);
        session.setEndTime(endTime);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setDeleted(false);
        this.save(session);

        return getSessionDetails(session.getId());
    }

    @Override
    @Transactional
    public SessionVO updateSession(Long sessionId, SessionDTO sessionDTO) {
        MovieSessions session = this.getById(sessionId);
        if (session == null || session.getDeleted()) throw new BusinessException("场次不存在");

        Movies movie = moviesMapper.selectById(sessionDTO.getMovieId());
        if (movie == null || movie.getDeleted()) throw new BusinessException("电影不存在");

        Halls hall = hallsMapper.selectById(sessionDTO.getHallId());
        if (hall == null || hall.getDeleted()) throw new BusinessException("影厅不存在");

        LocalDateTime startTime = sessionDTO.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        checkTimeConflict(sessionDTO.getHallId(), startTime, endTime, sessionId);

        BeanUtils.copyProperties(sessionDTO, session);
        session.setId(sessionId);
        session.setEndTime(endTime);
        session.setUpdatedAt(LocalDateTime.now());
        this.updateById(session);

        return getSessionDetails(session.getId());
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        MovieSessions session = this.getById(sessionId);
        if (session == null) return;

        // 检查场次是否已开始
        if (session.getSessionTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能删除已开始或已结束的场次");
        }

        session.setDeleted(true);
        session.setUpdatedAt(LocalDateTime.now());
        this.updateById(session);
    }

    @Override
    public List<SessionVO> getSessionsByMovieAndDate(Long movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        QueryWrapper<MovieSessions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("movie_id", movieId)
                .eq("is_deleted", false)
                .ge("start_time", startOfDay)
                .lt("start_time", endOfDay)
                .orderByAsc("start_time");

        List<MovieSessions> sessions = this.list(queryWrapper);
        return sessions.stream().map(this::mapToSessionVO).collect(Collectors.toList());
    }

    @Override
    public SessionVO getSessionDetails(Long sessionId) {
        MovieSessions session = this.getById(sessionId);
        if (session == null || session.getDeleted()) throw new BusinessException("场次不存在");
        return mapToSessionVO(session);
    }

    @Override
    public List<SeatVO> getSeatStatusForSession(Long sessionId) {
        MovieSessions session = this.getById(sessionId);
        if (session == null) throw new BusinessException("场次不存在");

        // 1. 获取影厅的所有座位
        List<Seats> allSeatsInHall = seatsService.list(
                new QueryWrapper<Seats>()
                        .eq("hall_id", session.getHallId())
                        .eq("is_deleted", false)
        );

        // 2. 获取座位在该场次的状态（从SeatsSessions表）
        List<com.example.movie_booking_backend.model.domain.SeatsSessions> seatsSessions = seatsSessionsMapper.selectList(
                new QueryWrapper<com.example.movie_booking_backend.model.domain.SeatsSessions>()
                        .eq("session_id", sessionId)
        );
        Map<Long, String> seatStatusMap = seatsSessions.stream()
                .collect(Collectors.toMap(com.example.movie_booking_backend.model.domain.SeatsSessions::getSeatId, com.example.movie_booking_backend.model.domain.SeatsSessions::getStatus));

        // 3. 转换为SeatVO并设置状态
        return allSeatsInHall.stream()
                .map(seat -> {
                    SeatVO seatVO = new SeatVO();
                    seatVO.setId(seat.getId());
                    seatVO.setRowNumber(seat.getSeatRow());
                    seatVO.setColumnNumber(seat.getSeatColumn());
                    seatVO.setSeatType(seat.getSeatType());
                    seatVO.setPriceMultiplier(seat.getPriceMultiplier());

                    // 从seatStatusMap获取状态，默认AVAILABLE
                    String status = seatStatusMap.getOrDefault(seat.getId(), "AVAILABLE");
                    seatVO.setStatus(status);
                    return seatVO;
                })
                .collect(Collectors.toList());
    }



    private void checkTimeConflict(Long hallId, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId) {
        QueryWrapper<MovieSessions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hall_id", hallId)
                .eq("is_deleted", false)
                .and(qw -> qw.lt("start_time", endTime).gt("end_time", startTime));

        if (excludeSessionId != null) {
            queryWrapper.ne("id", excludeSessionId);
        }

        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("该时间段影厅已被占用");
        }
    }
    @Override
    public List<SessionInfoVO> getSessionInfosByMovieId(Long movieId) {
        return movieSessionsMapper.findSessionInfoByMovieId(movieId);
    }
    private SessionVO mapToSessionVO(MovieSessions session) {
        SessionVO vo = new SessionVO();
        BeanUtils.copyProperties(session, vo);
        vo.setMovie(moviesMapper.selectById(session.getMovieId()));
        vo.setHall(hallsMapper.selectById(session.getHallId()));
        return vo;
    }


    @Autowired
    private IHallsService hallsService; // 添加这个依赖

    @Override
    public SeatsSessionsVO getSeatsForSelection(Long sessionId) {
        // 1. 验证场次是否存在并获取影厅ID
        MovieSessions session = this.getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }

        // 2. 获取影厅信息（用于totalSeats/totalRows/totalColumns）
        Halls hall = hallsMapper.selectById(session.getHallId());
        if (hall == null) {
            throw new BusinessException("影厅信息不存在");
        }

        // 3. 获取该场次的所有座位关联信息
        List<SeatsSessions> seatsSessionsList = seatsSessionsMapper.selectList(
                new QueryWrapper<SeatsSessions>()
                        .eq("session_id", sessionId)
                        .eq("is_deleted", false)
        );

        // 4. 提取所有座位ID
        List<Long> seatIds = seatsSessionsList.stream()
                .map(SeatsSessions::getSeatId)
                .collect(Collectors.toList());

        // 5. 批量查询座位详细信息
        List<Seats> seatsList = seatIds.isEmpty() ?
                Collections.emptyList() :
                seatsService.listByIds(seatIds);

        // 6. 转换为SeatSessionVO列表
        List<SeatSessionVO> seatSessionVOs = seatsList.stream()
                .map(seat -> {
                    // 找到对应的SeatsSessions记录
                    SeatsSessions sessionRecord = seatsSessionsList.stream()
                            .filter(ss -> ss.getSeatId().equals(seat.getId()))
                            .findFirst()
                            .orElse(null);

                    SeatSessionVO vo = new SeatSessionVO();
                    vo.setId(seat.getId()); // 或者 sessionRecord.getId() 根据需求决定
                    vo.setSeatId(seat.getId());
                    vo.setSessionId(sessionId);
                    vo.setRowNumber(seat.getSeatRow());
                    vo.setColumnNumber(seat.getSeatColumn());
                    vo.setSeatType(seat.getSeatType());
                    vo.setPriceMultiplier(seat.getPriceMultiplier());
                    vo.setStatus(sessionRecord != null ? sessionRecord.getStatus() : "AVAILABLE");
                    vo.setCreatedAt(sessionRecord != null ? sessionRecord.getCreatedAt() : null);
                    vo.setUpdatedAt(sessionRecord != null ? sessionRecord.getUpdatedAt() : null);
                    vo.setDeleted(sessionRecord != null ? sessionRecord.getDeleted() : false);
                    return vo;
                })
                .collect(Collectors.toList());

        // 7. 封装返回结果
        SeatsSessionsVO result = new SeatsSessionsVO();
        result.setSeatSessions(seatSessionVOs);
        result.setTotalSeats(hall.getTotalSeats());
        result.setTotalRows(hall.getTotalRows());
        result.setTotalColumns(hall.getTotalColumns());
        return result;
    }}

