package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.model.domain.Movies;
import com.example.movie_booking_backend.mapper.MoviesMapper;
import com.example.movie_booking_backend.model.dto.MovieDTO;
import com.example.movie_booking_backend.service.FileService;
import com.example.movie_booking_backend.service.IMoviesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * <p>
 * 电影表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class MoviesServiceImpl extends ServiceImpl<MoviesMapper, Movies> implements IMoviesService {

    @Autowired
    private FileService fileService;

    @Override
    @Transactional
    public Movies createMovie(MovieDTO movieDTO) {
        Movies movie = new Movies();
        BeanUtils.copyProperties(movieDTO, movie);
        movie.setStatus("EDITING"); // 初始状态为编辑中
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
        movie.setDeleted(false);
        this.save(movie);
        return movie;
    }

    @Override
    @Transactional
    public Movies updateMovie(Long movieId, MovieDTO movieDTO) {
        Movies existingMovie = this.getById(movieId);
        if (existingMovie == null || existingMovie.getDeleted()) {
            throw new BusinessException("电影不存在");
        }
        BeanUtils.copyProperties(movieDTO, existingMovie);
        existingMovie.setId(movieId);
        existingMovie.setUpdatedAt(LocalDateTime.now());
        this.updateById(existingMovie);
        return existingMovie;
    }

    @Override
    @Transactional
    public void updateMovieStatus(Long movieId, String status) {
        Movies movie = this.getById(movieId);
        if (movie == null || movie.getDeleted()) {
            throw new BusinessException("电影不存在");
        }
        // 简单校验下状态
        if (!"EDITING,SHOWING,OFFLINE".contains(status.toUpperCase())) {
            throw new BusinessException("无效的电影状态");
        }
        movie.setStatus(status);
        movie.setUpdatedAt(LocalDateTime.now());
        this.updateById(movie);
    }

    @Override
    public Page<Movies> listMovies(Page<Movies> page, String title, String status) {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);

        if (title != null && !title.isEmpty()) {
            queryWrapper.like("title", title);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        return this.page(page, queryWrapper);
    }

    @Override
    public String uploadPoster(MultipartFile file) throws IOException {
        return (String) fileService.uploadFile(file, "posters").get("url");
    }

    @Override
    public String uploadVideo(MultipartFile file) throws IOException {
        return (String) fileService.uploadFile(file, "videos").get("url");
    }
}
