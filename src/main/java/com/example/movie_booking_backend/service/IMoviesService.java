package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.model.domain.Movies;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.MovieDTO;
import com.example.movie_booking_backend.model.dto.ratingDTO;
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

    boolean deleteMovie(Long movieId);

    List<Map<String, Object>> getAllGenresWithCount();

    Page<Movies> listMoviesByGenre(Page<Movies> page, String genre);

    List<Map<String, Object>> getAllRegionsWithCount();

    Page<Movies> listMoviesByRegion(Page<Movies> page, String region);

    int deleteMoviesByGenre(String genre);

    int deleteMoviesByRegion(String region);

    int updateMoviesByGenre(String oldGenre, String newGenre);

    int updateMoviesByRegion(String oldRegion, String newRegion);

    List<Movies> getTop5Movies();

    Page<Movies> searchMovies(Page<Movies> page, String keyword);

    List<Movies> getRecommendedMovies(Long movieId, Integer limit);

    List<Movies> getBestBoxOfficeMovies();

    JsonResponse<String> rateMovie(ratingDTO dto);

    List<Movies> getRecommendMovies(Long userId);

    Map<String, Object> analyzeMovie();
    
    List<Map<String, Object>> analyzeGenreBoxOffice();
    
    List<Map<String, Object>> analyzeRegionBoxOffice();

    List<Movies> getMovies(String status);
}
