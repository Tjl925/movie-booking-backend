package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.OrdersMapper;
import com.example.movie_booking_backend.model.domain.Movies;
import com.example.movie_booking_backend.mapper.MoviesMapper;
import com.example.movie_booking_backend.model.domain.Orders;
import com.example.movie_booking_backend.model.dto.MovieDTO;
import com.example.movie_booking_backend.model.dto.ratingDTO;
import com.example.movie_booking_backend.service.IMoviesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.movie_booking_backend.service.IOrdersService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Value("${file-upload-path}")
    private String uploadBaseDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif"
    );

  @Autowired
  private OrdersMapper ordersMapper;
  @Autowired
  private IOrdersService ordersService;

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
    public String uploadPoster(Long movieId, MultipartFile file) {
        // 1. 验证电影存在性
        Movies movie = moviesMapper.selectById(movieId);
        if (movie == null || movie.getDeleted()) {
            throw new BusinessException("电影不存在或已被删除");
        }

        // 2. 验证文件有效性（仅图片）
        validatePosterFile(file);

        // 3. 生成存储路径
        String relativePath = generatePosterPath(movieId, file.getOriginalFilename());
        Path storagePath = Paths.get(uploadBaseDir, relativePath);

        try {
            // 4. 确保目录存在
            Files.createDirectories(storagePath.getParent());

            // 5. 存储文件
            file.transferTo(storagePath);

            // 6. 更新数据库
            movie.setPosterUrl(relativePath);
            moviesMapper.updateById(movie);

            return relativePath;
        } catch (IOException e) {
            log.error("海报存储失败", e);
            throw new BusinessException("海报上传失败，请重试");
        }
    }

    @Override
    public String uploadVideo(Long movieId, MultipartFile file) {
        // 1. 验证电影存在性
        Movies movie = moviesMapper.selectById(movieId);
        if (movie == null || movie.getDeleted()) {
            throw new BusinessException("电影不存在或已被删除");
        }

        // 2. 验证文件有效性（仅视频）
        validateVideoFile(file);

        // 3. 生成存储路径
        String relativePath = generateTrailerPath(movieId, file.getOriginalFilename());
        Path storagePath = Paths.get(uploadBaseDir, relativePath);

        try {
            // 4. 确保目录存在
            Files.createDirectories(storagePath.getParent());

            // 5. 存储文件
            file.transferTo(storagePath);

            // 6. 更新数据库
            movie.setTrailerUrl(relativePath);
            moviesMapper.updateById(movie);

            return relativePath;
        } catch (IOException e) {
            log.error("预告片存储失败", e);
            throw new BusinessException("预告片上传失败，请重试");
        }
    }

    // 验证海报文件
    private void validatePosterFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("海报文件不能为空");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持JPEG/PNG/GIF格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("海报大小不能超过5MB");
        }
    }

    // 验证视频文件
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/mpeg", "video/quicktime"
    );

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("视频文件不能为空");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_VIDEO_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持MP4/AVI/MPEG/MOV格式的视频");
        }
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new BusinessException("视频大小不能超过100MB");
        }
    }

    // 生成海报路径
    private String generatePosterPath(Long movieId, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = "movie_" + movieId + "_poster_" + System.currentTimeMillis() + extension;
        return "/posters/" + filename;
    }

    // 生成预告片路径
    private String generateTrailerPath(Long movieId, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = "movie_" + movieId + "_trailer_" + System.currentTimeMillis() + extension;
        return "/trailers/" + filename;
    }

    @Autowired
    private MoviesMapper moviesMapper;

    @Override
    public List<Movies> getTop5Movies() {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("rating")  // 按评分降序
                .last("LIMIT 5");      // 限制5条

        return moviesMapper.selectList(queryWrapper);
    }

    @Override
    public List<Movies> getBestBoxOfficeMovies() {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "NOW_SHOWING").orderByDesc("box_office")  // 按票房降序
                .last("LIMIT 10");      // 限制10条
        return moviesMapper.selectList(queryWrapper);
    }

    @Override
    public JsonResponse<String> rateMovie(ratingDTO dto) {
        try {
            // 1. 验证评分范围
            if (dto.getRating() < 1 || dto.getRating() > 10) {
                return JsonResponse.failure("评分必须在1-10之间");
            }

            // 2. 获取订单并验证
            Orders order = ordersMapper.selectById(dto.getOrderId());
            if (order == null || order.getDeleted()) {
                return JsonResponse.failure("订单不存在或已删除");
            }
            if (order.getIsRated()) {
                return JsonResponse.failure("该订单已评分");
            }
            if (!order.getStatus().equals("COMPLETED")) {
                return JsonResponse.failure("只有已完成的订单才能评分");
            }

            // 3. 获取电影并验证
            Movies movie = this.getById(dto.getMovieId());
            if (movie == null || movie.getDeleted()) {
                return JsonResponse.failure("电影不存在");
            }

            // 4. 计算新评分
            BigDecimal oldRating = movie.getRating() != null ? movie.getRating() : BigDecimal.ZERO;
            int oldCount = movie.getRatingCount() != null ? movie.getRatingCount() : 0;

            // 新评分 = (旧评分 * 旧评分数 + 新评分) / (旧评分数 + 1)
            BigDecimal newRating = oldRating.multiply(BigDecimal.valueOf(oldCount))
                    .add(BigDecimal.valueOf(dto.getRating()))
                    .divide(BigDecimal.valueOf(oldCount + 1), 2, RoundingMode.HALF_UP);

            // 5. 更新电影评分
            movie.setRating(newRating);
            movie.setRatingCount(oldCount + 1);
            this.updateById(movie);

            // 6. 更新订单评分状态
            order.setIsRated(true);
            ordersMapper.updateById(order);
            return JsonResponse.success("评分成功");
        } catch (Exception e) {
            log.error("评分失败", e);
            return JsonResponse.failure("评分失败: " + e.getMessage());
        }
    }

    @Override
    public List<Movies> getRecommendMovies(Long userId) {
        // 1. 获取用户所有有效订单中的电影（PAID/COMPLETED状态）
        List<Movies> watchedMovies = ordersService.getAllOrdersByUserId(userId).stream()
                .filter(order -> "PAID".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))
                .map(order -> order.getSession().getMovie())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 2. 获取候选电影
        List<Movies> candidates = getCandidateMovies().stream()
                .filter(m -> !watchedMovies.contains(m))
                .collect(Collectors.toList());
        double maxLogView = candidates.stream()
                .mapToDouble(m -> Math.log10(m.getViewCount() + 1))
                .max().orElse(1);  // 获取log10(观影人数最大值)

        // 3. 计算推荐电影（最多5部）
        List<Movies> recommendations = new ArrayList<>();

        if (!watchedMovies.isEmpty()) {
            // 3.1 有观看历史：基于偏好推荐
            UserPreference preference = analyzeUserPreferences(watchedMovies);
            recommendations = candidates.stream()
                    .map(movie -> new ScoredMovie(movie, calculateScore(movie, preference, maxLogView)))
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(5)
                    .map(ScoredMovie::getMovie)
                    .collect(Collectors.toList());
        }

        // 4. 如果推荐不足5部，用热门电影补足
        if (recommendations.size() < 5) {
            int needed = 5 - recommendations.size();
            List<Movies> topMovies = this.getTop5Movies().stream()
                    .filter(m -> !watchedMovies.contains(m)) // 确保不重复推荐
                    .limit(needed)
                    .collect(Collectors.toList());
            recommendations.addAll(topMovies);
        }

        return recommendations.size() > 5 ?
                recommendations.subList(0, 5) :
                recommendations;
    }


    // --- 辅助方法 ---
    private UserPreference analyzeUserPreferences(List<Movies> watchedMovies) {
        UserPreference preference = new UserPreference();

        // 分析最喜欢的3种类型
        Map<String, Long> genreCount = watchedMovies.stream()
                .flatMap(movie -> Arrays.stream(movie.getGenre().split(",")))
                .map(String::trim)
                .collect(Collectors.groupingBy(
                        genre -> genre,
                        Collectors.counting()
                ));

        preference.setFavoriteGenres(
                genreCount.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet())
        );

        // 分析最喜欢的导演
        Map<String, Long> directorCount = watchedMovies.stream()
                .collect(Collectors.groupingBy(
                        Movies::getDirector,
                        Collectors.counting()
                ));

        preference.setFavoriteDirectors(
                directorCount.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(2)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet())
        );

        return preference;
    }

    private List<Movies> getCandidateMovies() {
        return moviesMapper.selectList(
                new QueryWrapper<Movies>()
                        .eq("is_deleted", false)
        );
    }

    private double calculateScore(Movies movie, UserPreference preference,
                                  double maxLogView) {
        double score = 0;

        // 1. 类型匹配 (0~40)
        long genreMatch = Arrays.stream(movie.getGenre().split(","))
                .map(String::trim)
                .filter(preference.getFavoriteGenres()::contains)
                .count();
        double genreScore = (genreMatch / 3.0) * 40;
        score += genreScore;

        // 2. 导演匹配 (0~20)
        if (preference.getFavoriteDirectors().contains(movie.getDirector())) {
            score += 20;
        }

        // 3. 评分 (0~20) 假设rating最大10
        if (movie.getRating() != null) {
            double ratingScore = (movie.getRating().doubleValue() / 10.0) * 20;
            score += ratingScore;
        }

        // 4. 热度 (0~20) 需要传入当前批次最大log值
        if (movie.getViewCount() != null && maxLogView > 0) {
            double logView = Math.log10(movie.getViewCount() + 1);
            double heatScore = (logView / maxLogView) * 20;
            score += heatScore;
        }

        return score; // 0~100
    }

    // --- 辅助类 ---
    @Data
    private static class UserPreference {
        private Set<String> favoriteGenres;
        private Set<String> favoriteDirectors;
    }

    @Data
    @AllArgsConstructor
    private static class ScoredMovie {
        private Movies movie;
        private double score;
    }
    @Override
    public Page<Movies> searchMovies(Page<Movies> page, String keyword) {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", "ENDED");

        // 构建搜索条件
        queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or().like("director", keyword)
                .or().like("genre", keyword)
                .or().like("actors", keyword));

        // 先按相关性排序（标题匹配更相关），再按评分降序排序
        queryWrapper.orderByDesc("CASE WHEN title LIKE '%" + keyword + "%' THEN 1 ELSE 0 END")
                .orderByDesc("rating");

        return this.page(page, queryWrapper);
    }

    @Override
    public List<Movies> getRecommendedMovies(Long movieId, Integer limit) {
        // 1. 获取当前电影
        Movies currentMovie = this.getById(movieId);
        if (currentMovie == null || currentMovie.getDeleted()) {
            throw new BusinessException("电影不存在或已下架");
        }

        // 2. 获取所有候选电影（排除当前电影和已下架电影）
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id", movieId).ne("status", "ENDED");
        List<Movies> allMovies = this.list(queryWrapper);

        // 3. 计算每部电影的推荐得分
        List<Movies> recommendedMovies = allMovies.stream()
                .map(movie -> new MovieScore(movie, calculateMovieScore(currentMovie, movie)))
                .sorted(Comparator.comparingDouble(MovieScore::getScore).reversed())
                .limit(limit)
                .map(MovieScore::getMovie)
                .collect(Collectors.toList());

        return recommendedMovies;
    }


    // 辅助类：用于存储电影和得分的关联
    private static class MovieScore {
        private final Movies movie;
        private final double score;

        public MovieScore(Movies movie, double score) {
            this.movie = movie;
            this.score = score;
        }

        public Movies getMovie() {
            return movie;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * 计算电影推荐得分
     * 权重分配：题材50%，评分30%，地区20%
     */
    private double calculateMovieScore(Movies current, Movies candidate) {
        // 1. 题材匹配度（50%）
        double genreScore = calculateGenreScore(current.getGenre(), candidate.getGenre()) * 0.5;

        // 2. 评分相似度（30%）
        double ratingScore = calculateRatingScore(
                current.getRating().doubleValue(),
                candidate.getRating().doubleValue()) * 0.3;

        // 3. 地区匹配（20%）
        double countryScore = current.getCountry().equals(candidate.getCountry()) ? 0.2 : 0;

        return genreScore + ratingScore + countryScore;
    }

    /**
     * 计算题材匹配度
     */
    private double calculateGenreScore(String currentGenres, String candidateGenres) {
        if (currentGenres == null || candidateGenres == null) return 0;

        Set<String> currentSet = Arrays.stream(currentGenres.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> candidateSet = Arrays.stream(candidateGenres.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        // 计算共同题材数量占当前电影题材数量的比例
        long commonCount = currentSet.stream()
                .filter(candidateSet::contains)
                .count();

        return currentSet.isEmpty() ? 0 : (double) commonCount / currentSet.size();
    }

    /**
     * 计算评分相似度（使用高斯函数）
     */
    private double calculateRatingScore(double currentRating, double candidateRating) {
        double diff = Math.abs(currentRating - candidateRating);
        return Math.exp(-(diff * diff) / 2); // 标准差σ=1
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
        // 1. 查询所有未删除的电影及其类型
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", "ENDED");
        queryWrapper.select("id", "genre"); // 只查询ID和genre字段

        List<Movies> movies = this.baseMapper.selectList(queryWrapper);

        Map<String, Integer> genreCountMap = new HashMap<>();

        //  处理每部电影的类型
        for (Movies movie : movies) {
            if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
                String[] genres = movie.getGenre().split(",");

                // 统计每种类型
                for (String genre : genres) {
                    String trimmedGenre = genre.trim();
                    if (!trimmedGenre.isEmpty()) {
                        genreCountMap.put(trimmedGenre,
                                genreCountMap.getOrDefault(trimmedGenre, 0) + 1);
                    }
                }
            }
        }

        // 4. 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : genreCountMap.entrySet()) {
            Map<String, Object> genreMap = new HashMap<>();
            genreMap.put("name", entry.getKey());
            genreMap.put("movieCount", entry.getValue());
            result.add(genreMap);
        }

        return result;
    }

    @Override
    public Page<Movies> listMoviesByGenre(Page<Movies> page, String genre) {
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", "ENDED");

        if (genre != null && !genre.isEmpty()) {
            queryWrapper.like("genre", genre);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getAllRegionsWithCount() {
        // 查询所有未删除的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", "ENDED");
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
        queryWrapper.ne("status", "ENDED");

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
        queryWrapper.like("genre", genre).ne("status", "ENDED");
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
        queryWrapper.eq("country", region).ne("status", "ENDED");
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
        queryWrapper.like("genre", oldGenre).ne("status", "ENDED");
        List<Movies> moviesList = this.list(queryWrapper);

        if (moviesList.isEmpty()) {
            return 0;
        }

        // 批量更新电影类型
        for (Movies movie : moviesList) {
             String str=movie.getGenre();
            String str1=str.replace(oldGenre,newGenre);
            movie.setGenre(str1);
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
        queryWrapper.eq("country", oldRegion).ne("status", "ENDED");
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

    @Override
    @Transactional
    public Map<String, Object> analyzeMovie() {
        Map<String, Object> map = new HashMap<>();
        QueryWrapper<Movies> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.in("status", Arrays.asList("NOW_SHOWING", "UPCOMING"));
        // 查询总数
        Long count = moviesMapper.selectCount(queryWrapper1);
        map.put("movieCount", count);
        // 查询票房总和
        QueryWrapper<Movies> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.select("SUM(box_office) as totalBoxOffice");
        List<Map<String, Object>> result = moviesMapper.selectMaps(queryWrapper2);
        Object totalBoxOffice = result.isEmpty() ? 0 : result.get(0).get("totalBoxOffice");
        map.put("totalBoxOffice", totalBoxOffice == null ? 0 : totalBoxOffice);
        return map;
    }
    
    @Override
    @Transactional
    public List<Map<String, Object>> analyzeGenreBoxOffice() {
        // 查询所有未下架的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        List<Movies> moviesList = this.list(queryWrapper);
        
        // 统计每种类型的电影数量和票房
        Map<String, Integer> genreCountMap = new HashMap<>();
        Map<String, Double> genreBoxOfficeMap = new HashMap<>();
        
        for (Movies movie : moviesList) {
            String genreStr = movie.getGenre();
            if (genreStr != null && !genreStr.isEmpty()) {
                // 处理多个类型的情况，以逗号分隔
                String[] genres = genreStr.split(",");
                for (String genre : genres) {
                    genre = genre.trim();
                    if (!genre.isEmpty()) {
                        // 更新类型计数
                        genreCountMap.put(genre, genreCountMap.getOrDefault(genre, 0) + 1);
                        
                        // 更新类型票房
                        Double boxOffice = movie.getBoxOffice() != null ? movie.getBoxOffice().doubleValue() : 0.0;
                        genreBoxOfficeMap.put(genre, genreBoxOfficeMap.getOrDefault(genre, 0.0) + boxOffice);
                    }
                }
            }
        }
        
        // 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : genreCountMap.entrySet()) {
            String genre = entry.getKey();
            Map<String, Object> genreMap = new HashMap<>();
            genreMap.put("name", genre);
            genreMap.put("movieCount", entry.getValue());
            genreMap.put("boxOffice", genreBoxOfficeMap.getOrDefault(genre, 0.0));
            result.add(genreMap);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public List<Map<String, Object>> analyzeRegionBoxOffice() {
        // 查询所有未下架的电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        List<Movies> moviesList = this.list(queryWrapper);
        
        // 统计每个区域的电影数量和票房
        Map<String, Integer> regionCountMap = new HashMap<>();
        Map<String, Double> regionBoxOfficeMap = new HashMap<>();
        
        for (Movies movie : moviesList) {
            String region = movie.getCountry();
            if (region != null && !region.isEmpty()) {
                // 更新区域计数
                regionCountMap.put(region, regionCountMap.getOrDefault(region, 0) + 1);
                
                // 更新区域票房
                Double boxOffice = movie.getBoxOffice() != null ? movie.getBoxOffice().doubleValue() : 0.0;
                regionBoxOfficeMap.put(region, regionBoxOfficeMap.getOrDefault(region, 0.0) + boxOffice);
            }
        }
        
        // 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : regionCountMap.entrySet()) {
            String region = entry.getKey();
            Map<String, Object> regionMap = new HashMap<>();
            regionMap.put("name", region);
            regionMap.put("movieCount", entry.getValue());
            regionMap.put("boxOffice", regionBoxOfficeMap.getOrDefault(region, 0.0));
            result.add(regionMap);
        }
        
        return result;
    }

    @Override
    public List<Movies> getMovies(String status) {
        // 参数校验
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("状态参数不能为空");
        }

        // 根据状态获取电影
        QueryWrapper<Movies> queryWrapper = new QueryWrapper<>();

        switch (status.toLowerCase()) {
            case "now_showing":
                queryWrapper.eq("status", "NOW_SHOWING");
                break;
            case "upcoming":
                queryWrapper.eq("status", "UPCOMING");
                break;
            default:
                throw new IllegalArgumentException("无效的状态参数: " + status);
        }
        return moviesMapper.selectList(queryWrapper);
    }
}
