package com.example.movie_booking_backend.web.controller;

import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.annotation.RequireRole;
import com.example.movie_booking_backend.common.constants.PermissionConstants;
import com.example.movie_booking_backend.model.dto.SessionDTO;
import com.example.movie_booking_backend.model.vo.SeatVO;
import com.example.movie_booking_backend.model.vo.SessionVO;
import com.example.movie_booking_backend.service.IMovieSessionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-23
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
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<SessionVO> createSession(@Valid @RequestBody SessionDTO sessionDTO) {
        SessionVO sessionVO = movieSessionsService.createSession(sessionDTO);
        return JsonResponse.success(sessionVO, "场次创建成功");
    }

    @ApiOperation("【后台】更新场次")
    @PutMapping("/{id}")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<SessionVO> updateSession(@PathVariable Long id, @Valid @RequestBody SessionDTO sessionDTO) {
        SessionVO sessionVO = movieSessionsService.updateSession(id, sessionDTO);
        return JsonResponse.success(sessionVO, "场次更新成功");
    }

    @ApiOperation("【后台】删除场次")
    @DeleteMapping("/{id}")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<String> deleteSession(@PathVariable Long id) {
        movieSessionsService.deleteSession(id);
        return JsonResponse.successMessage("场次删除成功");
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

    @ApiOperation("【前台】获取某场次的座位图及状态")
    @GetMapping("/public/{id}/seats")
    public JsonResponse<List<SeatVO>> getSeatStatusForSession(@PathVariable Long id) {
        List<SeatVO> seats = movieSessionsService.getSeatStatusForSession(id);
        return JsonResponse.success(seats);
    }
}

