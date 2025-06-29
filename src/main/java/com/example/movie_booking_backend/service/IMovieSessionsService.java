package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.MovieSessions;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.SeatSelectionDTO;
import com.example.movie_booking_backend.model.dto.SeatStatusUpdateDTO;
import com.example.movie_booking_backend.model.dto.SessionDTO;
import com.example.movie_booking_backend.model.vo.ConflictCheckVO;
import com.example.movie_booking_backend.model.vo.SeatVO;
import com.example.movie_booking_backend.model.vo.SessionInfoVO;
import com.example.movie_booking_backend.model.vo.SeatsSessionsVO;
import com.example.movie_booking_backend.model.vo.SessionVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 创建场次
     * @param sessionDTO 场次数据
     * @return 创建后的场次信息
     */
    SessionVO createSession(SessionDTO sessionDTO);

    /**
     * 更新场次
     * @param sessionId 场次ID
     * @param sessionDTO 场次数据
     * @return 更新后的场次信息
     */
    SessionVO updateSession(Long sessionId, SessionDTO sessionDTO);

    /**
     * 删除场次
     * @param sessionId 场次ID
     */
    void deleteSession(Long sessionId);

    /**
     * 获取某电影某天的所有场次
     * @param movieId 电影ID
     * @param date 日期
     * @return 场次列表
     */
    List<SessionVO> getSessionsByMovieAndDate(Long movieId, LocalDate date);

    /**
     * 获取场次详情
     * @param sessionId 场次ID
     * @return 场次详情
     */
    SessionVO getSessionDetails(Long sessionId);

    /**
     * 获取某场次的座位状态
     * @param sessionId 场次ID
     * @return 座位状态列表
     */
    List<SeatVO> getSeatStatusForSession(Long sessionId);

    /**
     * 获取某电影的所有场次信息
     * @param movieId 电影ID
     * @return 场次信息列表
     */
    List<SessionInfoVO> getSessionInfosByMovieId(Long movieId);

    SeatsSessionsVO getSeatsForSelection(Long id);
    /**
     * 批量更新座位状态
     * @param updates 座位状态更新列表
     */
//    @Transactional
//    void updateSeatsStatus(List<SeatStatusUpdateDTO> updates);

    /**
     * 获取场次座位选择状态
     * @param id 场次ID
     * @return 座位选择状态
     */
//    SessionSeatsVO getSeatsForSelection(Long id);


    /**
     * 更新座位状态
     * @param dto 座位选择数据
     */
//    void updateSeatStatus(SeatSelectionDTO dto);

    /**
     * 检查时间冲突
     * @param hallId 影厅ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeSessionId 排除的场次ID（用于更新场次时）
     * @return 冲突检查结果
     */
    ConflictCheckVO checkTimeConflict(Long hallId, LocalDateTime startTime, LocalDateTime endTime, Long excludeSessionId);

    /**
     * 获取场次列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键字
     * @return 分页场次列表
     */
    Page<SessionVO> getSessionList(Integer page, Integer size, String keyword);

    /**
     * 获取某电影的所有场次
     * @param movieId 电影ID
     * @param status 状态过滤（可选）
     * @return 场次列表
     */
//    List<SessionVO> getSessionsByMovieId(Long movieId, String status);
}
