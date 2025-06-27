package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.movie_booking_backend.model.domain.Movies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MovieStatusScheduler {

    @Autowired
    private IMoviesService moviesService;

    /**
     * 每天凌晨1点执行，自动更新电影状态
     * 1. 将到了上映日期的电影从UPCOMING更新为NOW_SHOWING
     * 2. 将到了下映日期的电影从NOW_SHOWING更新为ENDED
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void updateMovieStatusTask() {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        
        // 更新即将上映到正在上映的电影
        updateUpcomingToNowShowing(today);
        
        // 更新正在上映到已下架的电影
        updateNowShowingToEnded(today);
    }
    
    /**
     * 将到了上映日期的电影从UPCOMING更新为NOW_SHOWING
     */
    private void updateUpcomingToNowShowing(LocalDate today) {
        QueryWrapper<Movies> upcomingQuery = new QueryWrapper<>();
        upcomingQuery.eq("status", "UPCOMING")
                .eq("is_deleted", false)
                .le("release_date", today); // 上映日期小于等于今天
        
        List<Movies> upcomingMovies = moviesService.list(upcomingQuery);
        
        for (Movies movie : upcomingMovies) {
            movie.setStatus("NOW_SHOWING");
            movie.setUpdatedAt(LocalDateTime.now());
        }
        
        if (!upcomingMovies.isEmpty()) {
            moviesService.updateBatchById(upcomingMovies);
        }
    }
    
    /**
     * 将到了下映日期的电影从NOW_SHOWING更新为ENDED
     */
    private void updateNowShowingToEnded(LocalDate today) {
        QueryWrapper<Movies> nowShowingQuery = new QueryWrapper<>();
        nowShowingQuery.eq("status", "NOW_SHOWING")
                .eq("is_deleted", false)
                .le("end_date", today); // 下映日期小于等于今天
        
        List<Movies> nowShowingMovies = moviesService.list(nowShowingQuery);
        
        for (Movies movie : nowShowingMovies) {
            movie.setStatus("ENDED");
            movie.setUpdatedAt(LocalDateTime.now());
        }
        
        if (!nowShowingMovies.isEmpty()) {
            moviesService.updateBatchById(nowShowingMovies);
        }
    }
}