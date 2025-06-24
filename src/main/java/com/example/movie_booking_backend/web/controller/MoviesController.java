package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.annotation.RequireRole;
import com.example.movie_booking_backend.common.constants.PermissionConstants;
import com.example.movie_booking_backend.model.domain.Movies;
import com.example.movie_booking_backend.model.dto.MovieDTO;
import com.example.movie_booking_backend.service.IMoviesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-23
 * @version v1.0
 */
@Api(tags = "电影管理")
@RestController
@RequestMapping("/api/movies")
public class MoviesController {

    @Autowired
    private IMoviesService moviesService;

    // ================= 后台管理接口 =================

    @ApiOperation("【后台】分页获取电影列表")
    @GetMapping("/admin")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<Page<Movies>> getMovieList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("电影标题") @RequestParam(required = false) String title,
            @ApiParam("状态 (EDITING, SHOWING, OFFLINE)") @RequestParam(required = false) String status) {
        Page<Movies> page = new Page<>(current, size);
        return JsonResponse.success(moviesService.listMovies(page, title, status));
    }

    @ApiOperation("【后台】创建电影")
    @PostMapping
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<Movies> createMovie(@Valid @RequestBody MovieDTO movieDTO) {
        Movies movie = moviesService.createMovie(movieDTO);
        return JsonResponse.success(movie, "电影创建成功");
    }

    @ApiOperation("【后台】更新电影信息")
    @PutMapping("/{id}")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<Movies> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDTO movieDTO) {
        Movies updatedMovie = moviesService.updateMovie(id, movieDTO);
        return JsonResponse.success(updatedMovie, "电影更新成功");
    }

    @ApiOperation("【后台】删除电影")
    @DeleteMapping("/{id}")
    @RequireRole({PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<String> deleteMovie(@PathVariable Long id) {
        moviesService.removeById(id); // 逻辑删除，依赖于MyBatis Plus的默认配置
        return JsonResponse.successMessage("电影删除成功");
    }

    @ApiOperation("【后台】更新电影状态")
    @PutMapping("/{id}/status")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<String> updateMovieStatus(@PathVariable Long id, @RequestParam String status) {
        moviesService.updateMovieStatus(id, status);
        return JsonResponse.successMessage("电影状态更新成功");
    }

    @ApiOperation("【后台】上传电影海报")
    @PostMapping("/poster/upload")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<String> uploadPoster(@RequestParam("file") MultipartFile file) {
        try {
            String url = moviesService.uploadPoster(file);
            return JsonResponse.success(url, "海报上传成功");
        } catch (Exception e) {
            return JsonResponse.failure("海报上传失败: " + e.getMessage());
        }
    }

    @ApiOperation("【后台】上传电影视频")
    @PostMapping("/video/upload")
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String url = moviesService.uploadVideo(file);
            return JsonResponse.success(url, "视频上传成功");
        } catch (Exception e) {
            return JsonResponse.failure("视频上传失败: " + e.getMessage());
        }
    }

    // ================= 前台公共接口 =================

    @ApiOperation("【前台】分页获取正在上映的电影列表")
    @GetMapping("/public/showing")
    public JsonResponse<Page<Movies>> getShowingMovies(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Page<Movies> page = new Page<>(current, size);
        return JsonResponse.success(moviesService.listMovies(page, null, "SHOWING"));
    }

    @ApiOperation("【前台】根据ID获取电影详情")
    @GetMapping("/public/{id}")
    public JsonResponse<Movies> getMovieById(@PathVariable Long id) {
        Movies movie = moviesService.getById(id);
        if (movie == null || movie.getDeleted() || !"SHOWING".equals(movie.getStatus())) {
            return JsonResponse.failure("电影不存在或未上映");
        }
        return JsonResponse.success(movie);
    }
}

