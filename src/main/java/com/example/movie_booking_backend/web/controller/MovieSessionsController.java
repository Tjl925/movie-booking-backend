package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.model.dto.SeatSelectionDTO;
import com.example.movie_booking_backend.model.dto.SessionDTO;
import com.example.movie_booking_backend.model.vo.*;
import com.example.movie_booking_backend.service.IMovieSessionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-28
 * @version v1.0
 */
@Api(tags = "场次管理")
@RestController
@RequestMapping("/api/sessions")
public class MovieSessionsController {

    @Autowired
    private IMovieSessionsService movieSessionsService;

    // ================= 后台管理接口 =================

    @ApiOperation("【后台】创建场次")
    @PostMapping
    public JsonResponse<SessionVO> createSession(@Valid @RequestBody SessionDTO sessionDTO) {
        SessionVO sessionVO = movieSessionsService.createSession(sessionDTO);
        return JsonResponse.success(sessionVO, "场次创建成功");
    }

    @ApiOperation("【后台】更新场次")
    @PutMapping("/{id}")
    public JsonResponse<SessionVO> updateSession(@PathVariable Long id, @Valid @RequestBody SessionDTO sessionDTO) {
        SessionVO sessionVO = movieSessionsService.updateSession(id, sessionDTO);
        return JsonResponse.success(sessionVO, "场次更新成功");
    }

    @ApiOperation("【后台】删除场次")
    @DeleteMapping("/{id}")
    public JsonResponse<String> deleteSession(@PathVariable Long id) {
        movieSessionsService.deleteSession(id);
        return JsonResponse.successMessage("场次删除成功");
    }

    /**
     * 后台管理接口 - 检查场次时间冲突
     */
    @ApiOperation("检查场次时间冲突")
    @GetMapping("/admin/check-conflict")
    public JsonResponse<ConflictCheckVO> checkTimeConflict(
            @ApiParam("影厅ID") @RequestParam("hallId") Long hallId,
            @ApiParam("开始时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam("startTime") String startTimeStr,
            @ApiParam("结束时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam("endTime") String endTimeStr,
            @ApiParam("排除的场次ID（用于更新场次时）") @RequestParam(value = "excludeSessionId", required = false) Long excludeSessionId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

        ConflictCheckVO conflictCheckVO = movieSessionsService.checkTimeConflict(hallId, startTime, endTime, excludeSessionId);
        return JsonResponse.success(conflictCheckVO);
    }

    // ================= 前台公共接口 =================

    @ApiOperation("【前台】获取某电影某天的所有场次")
    @GetMapping("/public/movie/{movieId}/date/{date}")
    public JsonResponse<List<SessionVO>> getSessionsForMovieAndDate(
            @PathVariable Long movieId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SessionVO> sessions = movieSessionsService.getSessionsByMovieAndDate(movieId, date);
        return JsonResponse.success(sessions);
    }

    @ApiOperation("【前台】获取场次详情")
    @GetMapping("/public/{id}")
    public JsonResponse<SessionVO> getSessionDetails(@PathVariable Long id) {
        SessionVO sessionVO = movieSessionsService.getSessionDetails(id);
        return JsonResponse.success(sessionVO);
    }

//    @ApiOperation("【前台】获取某场次的座位图及状态")
//    @GetMapping("/public/{id}/seats")
//    public JsonResponse<List<SeatVO>> getSeatStatusForSession(@PathVariable Long id) {
//        List<SeatVO> seats = movieSessionsService.getSeatStatusForSession(id);
//        return JsonResponse.success(seats);
//    }

    @ApiOperation("【前台】获取某电影的所有场次完整信息")
    @GetMapping("/public/movie/{movieId}/session-infos")
    public JsonResponse<List<SessionInfoVO>> getSessionInfos(
            @PathVariable Long movieId) {
        List<SessionInfoVO> result = movieSessionsService.getSessionInfosByMovieId(movieId);
        return JsonResponse.success(result);
    }

    @ApiOperation("【前台】获取场次座位选择状态")
    @GetMapping("/public/{id}/seat-selection")
    public JsonResponse<SeatsSessionsVO> getSeatsForSelection(@PathVariable Long id) {
        SeatsSessionsVO seats = movieSessionsService.getSeatsForSelection(id);
        return JsonResponse.success(seats);
    }

//    @ApiOperation("【前台】更新单个座位状态")
//    @PostMapping("/public/update-seat-status")
//    public JsonResponse<String> updateSeatStatus(@RequestBody SeatSelectionDTO dto) {
//        movieSessionsService.updateSeatStatus(dto);
//        return JsonResponse.successMessage("座位状态更新成功");
//    }

    @ApiOperation("【后台】获取场次列表")
    @GetMapping
    public JsonResponse<Page<SessionVO>> getSessionList (
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword){
        Page<SessionVO> pages = movieSessionsService.getSessionList(page, size, keyword);
        return JsonResponse.success(pages);
    }

//    @ApiOperation("【前台】获取电影的场次列表")
//    @GetMapping("/movie/{movieId}")
//    public JsonResponse<List<SessionVO>> getMovieSessionList(
//            @PathVariable Long movieId,
//            @RequestParam(value = "status", required = false) String status) {
//        List<SessionVO> sessions = movieSessionsService.getSessionsByMovieId(movieId, status);
//        return JsonResponse.success(sessions);
//    }
}

