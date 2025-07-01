package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.Movies;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.MovieDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface IMoviesService extends IService<Movies> {

    Movies createMovie(MovieDTO movieDTO);

    Movies updateMovie(Long movieId, MovieDTO movieDTO);

    void updateMovieStatus(Long movieId, String status);

    Page<Movies> listMovies(Page<Movies> page, String title, String status);

    String uploadPoster(Long id, MultipartFile file) throws IOException;

    String uploadVideo(Long id, MultipartFile file) throws IOException;

    /**
     * 逻辑删除电影
     *
     * @param movieId 电影ID
     * @return 是否删除成功
     */
    boolean deleteMovie(Long movieId);

    /**
     * 获取所有电影类型及其对应的电影数量
     *
     * @return 类型列表，包含类型名称和电影数量
     */
    List<Map<String, Object>> getAllGenresWithCount();

    /**
     * 根据类型分页获取电影列表
     *
     * @param page 分页参数
     * @param genre 电影类型
     * @return 分页电影列表
     */
    Page<Movies> listMoviesByGenre(Page<Movies> page, String genre);

    /**
     * 获取所有电影区域及其对应的电影数量
     *
     * @return 区域列表，包含区域名称和电影数量
     */
    List<Map<String, Object>> getAllRegionsWithCount();

    /**
     * 根据区域分页获取电影列表
     *
     * @param page 分页参数
     * @param region 电影区域
     * @return 分页电影列表
     */
    Page<Movies> listMoviesByRegion(Page<Movies> page, String region);

    /**
     * 根据类型批量逻辑删除电影
     *
     * @param genre 电影类型
     * @return 删除的电影数量
     */
    int deleteMoviesByGenre(String genre);

    /**
     * 根据区域批量逻辑删除电影
     *
     * @param region 电影区域
     * @return 删除的电影数量
     */
    int deleteMoviesByRegion(String region);

    /**
     * 更新电影类型
     *
     * @param oldGenre 原电影类型
     * @param newGenre 新电影类型
     * @return 更新的电影数量
     */
    int updateMoviesByGenre(String oldGenre, String newGenre);

    /**
     * 更新电影区域
     *
     * @param oldRegion 原电影区域
     * @param newRegion 新电影区域
     * @return 更新的电影数量
     */
    int updateMoviesByRegion(String oldRegion, String newRegion);

    List<Movies> getTop10Movies();

    Page<Movies> searchMovies(Page<Movies> page, String keyword);

    List<Movies> getRecommendedMovies(Long movieId, Integer limit);

    List<Movies> getBestBoxOfficeMovies();
}
