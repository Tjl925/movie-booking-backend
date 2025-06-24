package com.example.movie_booking_backend.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.service.IOperationLogsService;
import com.example.movie_booking_backend.model.domain.OperationLogs;


/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-23
 * @version v1.0
 */
@RestController
@RequestMapping("/api/operationLogs")
public class OperationLogsController {

    private final Logger logger = LoggerFactory.getLogger( OperationLogsController.class );

    @Autowired
    private IOperationLogsService operationLogsService;


    /**
    * 描述：根据Id 查询
    *
    */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<OperationLogs> getById(@PathVariable("id") Long id)throws Exception {
        OperationLogs operationLogs = operationLogsService.getById(id);
        return JsonResponse.success(operationLogs);
    }
}

