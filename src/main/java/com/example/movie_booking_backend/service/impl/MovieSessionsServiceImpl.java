package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private HallsServiceImpl hallsService;

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

        LocalDateTime startTime = sessionDTO.getSessionTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        // 检查时间冲突
        ConflictCheckVO conflictCheck = checkTimeConflict(sessionDTO.getHallId(), startTime, endTime, null);
        if (conflictCheck.getHasConflict()) {
            throw new BusinessException(conflictCheck.getMessage());
        }

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

        LocalDateTime startTime = sessionDTO.getSessionTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        // 检查时间冲突
        ConflictCheckVO conflictCheck = checkTimeConflict(sessionDTO.getHallId(), startTime, endTime, sessionId);
        if (conflictCheck.getHasConflict()) {
            throw new BusinessException(conflictCheck.getMessage());
        }

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
                .ge("session_time", startOfDay)
                .lt("session_time", endOfDay)
                .orderByAsc("session_time");

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


    @Override
    public ConflictCheckVO checkTimeConflict(Long hallId, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId) {
        ConflictCheckVO result = new ConflictCheckVO();
        result.setHasConflict(false);

        // 查询该影厅在时间范围内的所有场次（排除自身）
        QueryWrapper<MovieSessions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hall_id", hallId)
                .eq("is_deleted", false)
                .and(wrapper -> wrapper
                        .between("session_time", startTime, endTime)
                        .or()
                        .between("end_time", startTime, endTime)
                        .or()
                        .le("session_time", startTime)
                        .ge("end_time", endTime)
                );

        if (excludeSessionId != null) {
            queryWrapper.ne("id", excludeSessionId);
        }

        List<MovieSessions> conflictingSessions = this.list(queryWrapper);

        if (!conflictingSessions.isEmpty()) {
            result.setHasConflict(true);
            // 获取冲突的电影信息
            MovieSessions firstConflict = conflictingSessions.get(0);
            Movies movie = moviesMapper.selectById(firstConflict.getMovieId());

            String message = String.format("时间冲突：该影厅在 %s 到 %s 已有《%s》的场次安排",
                    firstConflict.getSessionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    firstConflict.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    movie != null ? movie.getTitle() : "未知电影");
            result.setMessage(message);
        }

        return result;
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


//    @Transactional
//    @Override
//    public void updateSeatsStatus(List<SeatStatusUpdateDTO> updates) {
//        for (SeatStatusUpdateDTO update : updates) {
//            Seats seat = seatsService.getById(update.getId());
//            if (seat != null) {
//                seat.setStatus(update.getStatus());
//                seat.setUpdatedAt(LocalDateTime.now());
//                seatsService.updateById(seat);
//            }
//        }
//    }

//    @Override
//    public SessionSeatsVO getSeatsForSelection(Long sessionId) {
//        // 1. 验证场次是否存在并获取影厅ID
//        MovieSessions session = this.getById(sessionId);
//        if (session == null) {
//            throw new BusinessException("场次不存在");
//        }
//
//        // 2. 获取影厅信息
//        Halls hall = hallsMapper.selectById(session.getHallId());
//        if (hall == null) {
//            throw new BusinessException("影厅信息不存在");
//        }
//
//        // 3. 获取影厅所有有效座位
//        List<Seats> allSeatsInHall = seatsService.list(
//                new QueryWrapper<Seats>()
//                        .eq("hall_id", session.getHallId())
//                        .eq("is_deleted", false)
//        );
//        // 5. 转换并设置座位状态（完全手动映射）
//        List<SeatVO> seatVOs = allSeatsInHall.stream()
//                .map(seat -> {
//                    SeatVO seatVO = new SeatVO();
//                    // 手动映射所有字段
//                    seatVO.setId(seat.getId());
//                    seatVO.setRowNumber(seat.getSeatRow());     // 关键修改点
//                    seatVO.setColumnNumber(seat.getSeatColumn()); // 关键修改点
//
//                    // 状态设置
//                    switch (seat.getStatus()) {
//                        case "RESERVED":
//                            seatVO.setStatus("RESERVED");
//                            break;
//                        case "MAINTENANCE":
//                            seatVO.setStatus("MAINTENANCE");
//                            break;
//                        case "OCCUPIED":
//                            seatVO.setStatus("OCCUPIED");
//                            break;
//                        default:
//                            seatVO.setStatus("AVAILABLE");
//                    }
//                    return seatVO;
//                })
//                .collect(Collectors.toList());
//
//        // 6. 封装返回结果
//        SessionSeatsVO result = new SessionSeatsVO();
//        result.setSeats(seatVOs);
//        result.setTotalSeats(hall.getTotalSeats());
//        result.setTotalRows(hall.getTotalRows());
//        result.setTotalColumns(hall.getTotalColumns());
//
//        return result;
//    }

//    @Transactional
//    @Override
//    public void updateSeatStatus(SeatSelectionDTO dto) {
//        if (dto.getSeatId() == null || dto.getStatus() == null) {
//            throw new BusinessException("座位ID和状态不能为空");
//        }
//
//        Seats seat = seatsService.getById(dto.getSeatId());
//        if (seat == null) {
//            throw new BusinessException("座位不存在");
//        }
//
//        // 验证状态转换是否合法
//        if ("RESERVED".equals(dto.getStatus())) {
//            if (!"AVAILABLE".equals(seat.getStatus())) {
//                throw new BusinessException("只能预定可用座位");
//            }
//        }
//
//        seat.setStatus(dto.getStatus());
//        seat.setUpdatedAt(LocalDateTime.now());
//        seatsService.updateById(seat);
//    }

    @Override
    public Page<SessionVO> getSessionList(Integer page, Integer size, String keyword) {
        // 构建查询条件
        QueryWrapper<MovieSessions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);

        // 如果有关键字，则搜索电影标题或影厅名称
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 这里需要使用子查询或连接查询，简化起见，我们先获取所有符合条件的场次ID
            List<Long> sessionIds = movieSessionsMapper.findSessionIdsByKeyword(keyword);
            if (!sessionIds.isEmpty()) {
                queryWrapper.in("id", sessionIds);
            } else {
                // 如果没有匹配的结果，返回空列表
                return new Page<>();
            }
        }

        // 按开始时间降序排序
        queryWrapper.orderByDesc("session_time");

        // 分页查询
        Page<MovieSessions> pageParam = new Page<>(page, size);
        Page<MovieSessions> pageResult = this.page(pageParam, queryWrapper);

        // 转换为VO对象
        List<SessionVO> sessionVOs = pageResult.getRecords().stream()
                .map(this::mapToSessionVO)
                .collect(Collectors.toList());

        Page<SessionVO> voPage = new Page<>();
        voPage.setRecords(sessionVOs);
        voPage.setTotal(pageResult.getTotal());
        voPage.setSize(pageResult.getSize());
        voPage.setCurrent(pageResult.getCurrent());
        voPage.setPages(pageResult.getPages());
        return voPage;
    }

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
    }


}

