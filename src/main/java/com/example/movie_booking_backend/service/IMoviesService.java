package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.Movies;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.MovieDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    String uploadPoster(MultipartFile file) throws IOException;

    String uploadVideo(MultipartFile file) throws IOException;
}
