package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.OperationLogs;
import com.example.movie_booking_backend.mapper.OperationLogsMapper;
import com.example.movie_booking_backend.service.IOperationLogsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class OperationLogsServiceImpl extends ServiceImpl<OperationLogsMapper, OperationLogs> implements IOperationLogsService {

}
