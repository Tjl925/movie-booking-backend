package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
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

import java.util.List;
import java.util.Map;

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
    public JsonResponse<Page<Movies>> getMovieList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("电影标题") @RequestParam(required = false) String title,
            @ApiParam("状态 (UPCOMING, NOW_SHOWING, ENDED)") @RequestParam(required = false) String status) {
        Page<Movies> page = new Page<>(current, size);
        return JsonResponse.success(moviesService.listMovies(page, title, status));
    }

    @ApiOperation("【后台】创建电影")
    @PostMapping
    public JsonResponse<Movies> createMovie(@Valid @RequestBody MovieDTO movieDTO) {
        Movies movie = moviesService.createMovie(movieDTO);
        return JsonResponse.success(movie, "电影创建成功");
    }

    @ApiOperation("【后台】更新电影信息")
    @PutMapping("/{id}")
    public JsonResponse<Movies> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDTO movieDTO) {
        Movies updatedMovie = moviesService.updateMovie(id, movieDTO);
        return JsonResponse.success(updatedMovie, "电影更新成功");
    }

    @ApiOperation("【后台】删除电影")
    @DeleteMapping("/{id}")
    public JsonResponse<String> deleteMovie(@PathVariable Long id) {
        boolean result = moviesService.deleteMovie(id); // 使用自定义的逻辑删除方法
        if (result) {
            return JsonResponse.successMessage("电影删除成功");
        } else {
            return JsonResponse.failure("电影删除失败");
        }
    }

    @ApiOperation("【后台】更新电影状态")
    @PutMapping("/{id}/status")
    public JsonResponse<String> updateMovieStatus(@PathVariable Long id, @RequestParam String status) {
        moviesService.updateMovieStatus(id, status);
        return JsonResponse.successMessage("电影状态更新成功");
    }

    @ApiOperation("【后台】上传电影海报")
    @PostMapping("/poster/upload")
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
        return JsonResponse.success(moviesService.listMovies(page, null, "NOW_SHOWING"));
    }

    @ApiOperation("【前台】根据ID获取电影详情")
    @GetMapping("/public/{id}")
    public JsonResponse<Movies> getMovieById(@PathVariable Long id) {
        Movies movie = moviesService.getById(id);
        System.out.println(movie);
        if (movie == null || movie.getDeleted() || !"NOW_SHOWING".equals(movie.getStatus())) {
            return JsonResponse.failure("电影不存在或未上映");
        }
        return JsonResponse.success(movie);
    }

    // ================= 电影类型相关接口 =================

    @ApiOperation("获取所有电影类型及其电影数量")
    @GetMapping("/genres")
    public JsonResponse<List<Map<String, Object>>> getAllGenres() {
        return JsonResponse.success(moviesService.getAllGenresWithCount());
    }

    @ApiOperation("根据类型分页获取电影列表")
    @GetMapping("/genres/{genre}")
    public JsonResponse<Page<Movies>> getMoviesByGenre(
            @PathVariable String genre,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Page<Movies> page = new Page<>(current, size);
        return JsonResponse.success(moviesService.listMoviesByGenre(page, genre));
    }

    @ApiOperation("根据类型批量删除电影")
    @DeleteMapping("/genres/{genre}")
    public JsonResponse<String> deleteMoviesByGenre(@PathVariable String genre) {
        int count = moviesService.deleteMoviesByGenre(genre);
        return JsonResponse.success(null, "成功删除 " + count + " 部电影");
    }

    @ApiOperation("更新电影类型")
    @PutMapping("/genres/{oldGenre}")
    public JsonResponse<String> updateMoviesByGenre(
            @PathVariable String oldGenre,
            @RequestParam String newGenre) {
        int count = moviesService.updateMoviesByGenre(oldGenre, newGenre);
        return JsonResponse.success(null, "成功更新 " + count + " 部电影的类型");
    }

    // ================= 电影区域相关接口 =================

    @ApiOperation("获取所有电影区域及其电影数量")
    @GetMapping("/regions")
    public JsonResponse<List<Map<String, Object>>> getAllRegions() {
        return JsonResponse.success(moviesService.getAllRegionsWithCount());
    }

    @ApiOperation("根据区域分页获取电影列表")
    @GetMapping("/regions/{region}")
    public JsonResponse<Page<Movies>> getMoviesByRegion(
            @PathVariable String region,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Page<Movies> page = new Page<>(current, size);
        return JsonResponse.success(moviesService.listMoviesByRegion(page, region));
    }

    @ApiOperation("根据区域批量删除电影")
    @DeleteMapping("/regions/{region}")
    public JsonResponse<String> deleteMoviesByRegion(@PathVariable String region) {
        int count = moviesService.deleteMoviesByRegion(region);
        return JsonResponse.success(null, "成功删除 " + count + " 部电影");
    }

    @ApiOperation("更新电影区域")
    @PutMapping("/regions/{oldRegion}")
    public JsonResponse<String> updateMoviesByRegion(
            @PathVariable String oldRegion,
            @RequestParam String newRegion) {
        int count = moviesService.updateMoviesByRegion(oldRegion, newRegion);
        return JsonResponse.success(null, "成功更新 " + count + " 部电影的区域");
    }
    @GetMapping("public/top10")
    public JsonResponse<List<Movies>> getTop10Movies(){
        return JsonResponse.success(moviesService.getTop10Movies());
    }

    @ApiOperation("【前台】搜索电影")
    @GetMapping("/public/search")
    public JsonResponse<Page<Movies>> getSearchList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("搜索关键词") @RequestParam String keyword) {
        Page<Movies> page = new Page<>(current, size);
        return JsonResponse.success(moviesService.searchMovies(page, keyword));
    }
}

