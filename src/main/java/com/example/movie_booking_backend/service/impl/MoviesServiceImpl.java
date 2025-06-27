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
import java.util.*;

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
        movie.setStatus("UPCOMING"); // 设置初始状态为即将上映
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
        // 校验状态是否为三状态模型中的一种
        if (!"UPCOMING,NOW_SHOWING,ENDED".contains(status.toUpperCase())) {
            throw new BusinessException("无效的电影状态，状态必须是UPCOMING、NOW_SHOWING或ENDED");
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
    
    @Override
    @Transactional
    public boolean deleteMovie(Long movieId) {
        // 查询电影是否存在
        Movies movie = this.getById(movieId);
        if (movie == null) {
            throw new BusinessException("电影不存在");
        }
        
        // 如果已经被删除，直接返回成功
        if (Objects.equals(movie.getStatus(), "ENDED")) {
            return true;
        }

        // 将状态设置为下线
        movie.setStatus("ENDED");
        movie.setUpdatedAt(LocalDateTime.now());
        
        // 更新数据库
        return this.updateById(movie);
    }
    
    @Override
    public List<Map<String, Object>> getAllGenresWithCount() {
        // 查询所有未删除的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.select("genre", "COUNT(*) as count");
        queryWrapper.groupBy("genre");
        
        // 使用Mapper执行自定义SQL查询
        List<Map<String, Object>> result = this.baseMapper.selectMaps(queryWrapper);
        
        // 处理结果，确保返回格式一致
        List<Map<String, Object>> genreList = new ArrayList<>();
        for (Map<String, Object> item : result) {
            Map<String, Object> genreMap = new HashMap<>();
            genreMap.put("name", item.get("genre"));
            genreMap.put("movieCount", item.get("count"));
            genreList.add(genreMap);
        }
        
        return genreList;
    }
    
    @Override
    public Page<Movies> listMoviesByGenre(Page<Movies> page, String genre) {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        
        if (genre != null && !genre.isEmpty()) {
            queryWrapper.eq("genre", genre);
        }
        
        return this.page(page, queryWrapper);
    }
    
    @Override
    public List<Map<String, Object>> getAllRegionsWithCount() {
        // 查询所有未删除的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.select("country", "COUNT(*) as count");
        queryWrapper.groupBy("country");
        
        // 使用Mapper执行自定义SQL查询
        List<Map<String, Object>> result = this.baseMapper.selectMaps(queryWrapper);
        
        // 处理结果，确保返回格式一致
        List<Map<String, Object>> regionList = new ArrayList<>();
        for (Map<String, Object> item : result) {
            Map<String, Object> regionMap = new HashMap<>();
            regionMap.put("name", item.get("country"));
            regionMap.put("movieCount", item.get("count"));
            regionList.add(regionMap);
        }
        
        return regionList;
    }
    
    @Override
    public Page<Movies> listMoviesByRegion(Page<Movies> page, String region) {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        
        if (region != null && !region.isEmpty()) {
            queryWrapper.eq("country", region);
        }
        
        return this.page(page, queryWrapper);
    }
    
    @Override
    @Transactional
    public int deleteMoviesByGenre(String genre) {
        if (genre == null || genre.isEmpty()) {
            throw new BusinessException("电影类型不能为空");
        }
        
        // 查询符合条件的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("genre", genre).eq("is_deleted", false);
        List<Movies> moviesList = this.list(queryWrapper);
        
        if (moviesList.isEmpty()) {
            return 0;
        }
        
        // 批量更新电影状态为已删除
        for (Movies movie : moviesList) {
            movie.setStatus("ENDED");
            movie.setUpdatedAt(LocalDateTime.now());
        }
        
        // 批量更新
        this.updateBatchById(moviesList);
        
        return moviesList.size();
    }
    
    @Override
    @Transactional
    public int deleteMoviesByRegion(String region) {
        if (region == null || region.isEmpty()) {
            throw new BusinessException("电影区域不能为空");
        }
        
        // 查询符合条件的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("country", region).eq("is_deleted", false);
        List<Movies> moviesList = this.list(queryWrapper);
        
        if (moviesList.isEmpty()) {
            return 0;
        }
        
        // 批量更新电影状态为已删除
        for (Movies movie : moviesList) {
            movie.setStatus("ENDED");
            movie.setUpdatedAt(LocalDateTime.now());
        }
        
        // 批量更新
        this.updateBatchById(moviesList);
        
        return moviesList.size();
    }
    
    @Override
    @Transactional
    public int updateMoviesByGenre(String oldGenre, String newGenre) {
        if (oldGenre == null || oldGenre.isEmpty()) {
            throw new BusinessException("原电影类型不能为空");
        }
        
        if (newGenre == null || newGenre.isEmpty()) {
            throw new BusinessException("新电影类型不能为空");
        }
        
        // 查询符合条件的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("genre", oldGenre).eq("is_deleted", false);
        List<Movies> moviesList = this.list(queryWrapper);
        
        if (moviesList.isEmpty()) {
            return 0;
        }
        
        // 批量更新电影类型
        for (Movies movie : moviesList) {
            movie.setGenre(newGenre);
            movie.setUpdatedAt(LocalDateTime.now());
        }
        
        // 批量更新
        this.updateBatchById(moviesList);
        
        return moviesList.size();
    }
    
    @Override
    @Transactional
    public int updateMoviesByRegion(String oldRegion, String newRegion) {
        if (oldRegion == null || oldRegion.isEmpty()) {
            throw new BusinessException("原电影区域不能为空");
        }
        
        if (newRegion == null || newRegion.isEmpty()) {
            throw new BusinessException("新电影区域不能为空");
        }
        
        // 查询符合条件的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("country", oldRegion).eq("is_deleted", false);
        List<Movies> moviesList = this.list(queryWrapper);
        
        if (moviesList.isEmpty()) {
            return 0;
        }
        
        // 批量更新电影区域
        for (Movies movie : moviesList) {
            movie.setCountry(newRegion);
            movie.setUpdatedAt(LocalDateTime.now());
        }
        
        // 批量更新
        this.updateBatchById(moviesList);
        
        return moviesList.size();
    }
}
