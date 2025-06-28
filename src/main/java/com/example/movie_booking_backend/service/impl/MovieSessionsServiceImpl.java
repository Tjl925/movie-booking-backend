package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.HallsMapper;
import com.example.movie_booking_backend.mapper.MoviesMapper;
import com.example.movie_booking_backend.mapper.OrderItemsMapper;
import com.example.movie_booking_backend.model.domain.*;
import com.example.movie_booking_backend.mapper.MovieSessionsMapper;
import com.example.movie_booking_backend.model.dto.SeatSelectionDTO;
import com.example.movie_booking_backend.model.dto.SeatStatusUpdateDTO;
import com.example.movie_booking_backend.model.dto.SessionDTO;
import com.example.movie_booking_backend.model.vo.SeatVO;
import com.example.movie_booking_backend.model.vo.SessionInfo;
import com.example.movie_booking_backend.model.vo.SessionSeatsVO;
import com.example.movie_booking_backend.model.vo.SessionVO;
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
import java.util.List;
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
        // 2. 转换为SeatVO并设置状态
        return allSeatsInHall.stream()
                .map(seat -> {
                    SeatVO seatVO = new SeatVO();
                    // 手动设置字段映射
                    seatVO.setId(seat.getId());
                    seatVO.setRowNumber(seat.getSeatRow());
                    seatVO.setColumnNumber(seat.getSeatColumn());
                    String dbStatus = seat.getStatus();
                        // 根据座位原始状态转换
                    System.out.println("wocaonima");
                    switch (seat.getStatus()) {
                        case "RESERVED":
                            seatVO.setStatus("RESERVED");
                            break;
                        case "MAINTENANCE":
                             seatVO.setStatus("MAINTENANCE");
                             break;
                        case "OCCUPIED":
                             seatVO.setStatus("OCCUPIE");
                             break;
                        default:
                             seatVO.setStatus("AVAILABLE");
                        }

                    System.out.println("座位ID: " + seat.getId() + ", 数据库status: " + dbStatus + ", 类型: " + dbStatus.getClass());
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
    public List<SessionInfo> getSessionInfosByMovieId(Long movieId) {
        return movieSessionsMapper.findSessionInfoByMovieId(movieId);
    }
    private SessionVO mapToSessionVO(MovieSessions session) {
        SessionVO vo = new SessionVO();
        BeanUtils.copyProperties(session, vo);
        vo.setMovie(moviesMapper.selectById(session.getMovieId()));
        vo.setHall(hallsMapper.selectById(session.getHallId()));
        return vo;
    }


    @Transactional
    @Override
    public void updateSeatsStatus(List<SeatStatusUpdateDTO> updates) {
        for (SeatStatusUpdateDTO update : updates) {
            Seats seat = seatsService.getById(update.getId());
            if (seat != null) {
                seat.setStatus(update.getStatus());
                seat.setUpdatedAt(LocalDateTime.now());
                seatsService.updateById(seat);
            }
        }
    }
    @Autowired
    private IHallsService hallsService; // 添加这个依赖

    @Override
    public SessionSeatsVO getSeatsForSelection(Long sessionId) {
        // 1. 验证场次是否存在并获取影厅ID
        MovieSessions session = this.getById(sessionId);
        if (session == null) {
            throw new BusinessException("场次不存在");
        }

        // 2. 获取影厅信息
        Halls hall = hallsMapper.selectById(session.getHallId());
        if (hall == null) {
            throw new BusinessException("影厅信息不存在");
        }

        // 3. 获取影厅所有有效座位
        List<Seats> allSeatsInHall = seatsService.list(
                new QueryWrapper<Seats>()
                        .eq("hall_id", session.getHallId())
                        .eq("is_deleted", false)
        );
        // 5. 转换并设置座位状态（完全手动映射）
        List<SeatVO> seatVOs = allSeatsInHall.stream()
                .map(seat -> {
                    SeatVO seatVO = new SeatVO();
                    // 手动映射所有字段
                    seatVO.setId(seat.getId());
                    seatVO.setRowNumber(seat.getSeatRow());     // 关键修改点
                    seatVO.setColumnNumber(seat.getSeatColumn()); // 关键修改点

                    // 状态设置
                    switch (seat.getStatus()) {
                        case "RESERVED":
                            seatVO.setStatus("RESERVED");
                            break;
                        case "MAINTENANCE":
                            seatVO.setStatus("MAINTENANCE");
                            break;
                        case "OCCUPIED":
                            seatVO.setStatus("OCCUPIED");
                            break;
                        default:
                            seatVO.setStatus("AVAILABLE");
                    }
                    return seatVO;
                })
                .collect(Collectors.toList());

        // 6. 封装返回结果
        SessionSeatsVO result = new SessionSeatsVO();
        result.setSeats(seatVOs);
        result.setTotalSeats(hall.getTotalSeats());
        result.setTotalRows(hall.getTotalRows());
        result.setTotalColumns(hall.getTotalColumns());

        return result;
    }

    @Transactional
    @Override
    public void updateSeatStatus(SeatSelectionDTO dto) {
        if (dto.getSeatId() == null || dto.getStatus() == null) {
            throw new BusinessException("座位ID和状态不能为空");
        }

        Seats seat = seatsService.getById(dto.getSeatId());
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }

        // 验证状态转换是否合法
        if ("RESERVED".equals(dto.getStatus())) {
            if (!"AVAILABLE".equals(seat.getStatus())) {
                throw new BusinessException("只能预定可用座位");
            }
        }

        seat.setStatus(dto.getStatus());
        seat.setUpdatedAt(LocalDateTime.now());
        seatsService.updateById(seat);
    }

}
