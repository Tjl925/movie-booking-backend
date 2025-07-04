package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.service.IHallsService;
import com.example.movie_booking_backend.model.domain.Halls;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 *
 *  影厅管理前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-28
 * @version v1.0
 */
@Api(tags = "影厅管理")
@RestController
@RequestMapping("/api/halls")
public class HallsController {

    private final Logger logger = LoggerFactory.getLogger(HallsController.class);

    @Autowired
    private IHallsService hallsService;

    /**
     * 获取所有影厅列表
     */
//    @ApiOperation("获取所有影厅列表")
//    @GetMapping
//    public JsonResponse<Page<Halls>> getHallList(
//            @RequestParam(value = "page", defaultValue = "1") Integer page,
//            @RequestParam(value = "size", defaultValue = "10") Integer size,
//            @RequestParam(value = "name", required = false) String name,
//            @RequestParam(value = "type", required = false) String type,
//            @RequestParam(value = "status", required = false) String status) {
//
//        QueryWrapper<Halls> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("is_deleted", false);
//
//        if (name != null && !name.isEmpty()) {
//            queryWrapper.like("hall_name", name);
//        }
//
//        if (type != null && !type.isEmpty()) {
//            queryWrapper.eq("hall_type", type);
//        }
//
//        if (status != null && !status.isEmpty()) {
//            queryWrapper.eq("status", status);
//        }
//
//        queryWrapper.orderByDesc("created_at");
//
//        Page<Halls> pageResult = hallsService.page(new Page<>(page, size), queryWrapper);
//        return JsonResponse.success(pageResult);
//    }

    /**
     * 获取可用的影厅列表（状态为ACTIVE的影厅）
     */
    @ApiOperation("获取可用的影厅列表")
    @GetMapping("/active")
    public JsonResponse<List<Halls>> getActiveHalls() {
        QueryWrapper<Halls> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                .eq("is_deleted", false);
        
        List<Halls> activeHalls = hallsService.list(queryWrapper);
        return JsonResponse.success(activeHalls);
    }

    /**
     * 根据Id查询影厅
     */
    @ApiOperation("根据Id查询影厅")
    @GetMapping("/{id}")
    public JsonResponse<Halls> getById(@PathVariable("id") Long id) {
        Halls hall = hallsService.getById(id);
        if (hall == null || hall.getDeleted()) {
            throw new BusinessException("影厅不存在");
        }
        return JsonResponse.success(hall);
    }

//    /**
//     * 创建影厅
//     */
//    @ApiOperation("创建影厅")
//    @PostMapping
//    public JsonResponse<Halls> createHall(@Valid @RequestBody Halls hall) {
//        hall.setCreatedAt(LocalDateTime.now());
//        hall.setUpdatedAt(LocalDateTime.now());
//        hall.setDeleted(false);
//
//        hallsService.save(hall);
//        return JsonResponse.success(hall, "影厅创建成功");
//    }
//
//    /**
//     * 更新影厅
//     */
//    @ApiOperation("更新影厅")
//    @PutMapping("/{id}")
//    public JsonResponse<Halls> updateHall(@PathVariable("id") Long id, @Valid @RequestBody Halls hall) {
//        Halls existingHall = hallsService.getById(id);
//        if (existingHall == null || existingHall.getDeleted()) {
//            throw new BusinessException("影厅不存在");
//        }
//
//        hall.setId(id);
//        hall.setUpdatedAt(LocalDateTime.now());
//        hallsService.updateById(hall);
//
//        return JsonResponse.success(hall, "影厅更新成功");
//    }
//
//    /**
//     * 删除影厅
//     */
//    @ApiOperation("删除影厅")
//    @DeleteMapping("/{id}")
//    public JsonResponse<String> deleteHall(@PathVariable("id") Long id) {
//        Halls hall = hallsService.getById(id);
//        if (hall == null || hall.getDeleted()) {
//            throw new BusinessException("影厅不存在");
//        }
//
//        hall.setDeleted(true);
//        hall.setUpdatedAt(LocalDateTime.now());
//        hallsService.updateById(hall);
//
//        return JsonResponse.successMessage("影厅删除成功");
//    }
//
//    /**
//     * 更新影厅状态
//     */
//    @ApiOperation("更新影厅状态")
//    @PutMapping("/{id}/status")
//    public JsonResponse<Halls> updateHallStatus(
//            @PathVariable("id") Long id,
//            @RequestParam("status") String status) {
//
//        Halls hall = hallsService.getById(id);
//        if (hall == null || hall.getDeleted()) {
//            throw new BusinessException("影厅不存在");
//        }
//
//        // 验证状态值是否合法
//        if (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("MAINTENANCE")) {
//            throw new BusinessException("无效的状态值");
//        }
//
//        hall.setStatus(status);
//        hall.setUpdatedAt(LocalDateTime.now());
//        hallsService.updateById(hall);
//
//        return JsonResponse.success(hall, "影厅状态更新成功");
//    }
}