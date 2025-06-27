package com.example.movie_booking_backend.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.service.IHallsService;
import com.example.movie_booking_backend.model.domain.Halls;


/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-28
 * @version v1.0
 */
@RestController
@RequestMapping("/api/halls")
public class HallsController {

    private final Logger logger = LoggerFactory.getLogger( HallsController.class );

    @Autowired
    private IHallsService hallsService;


    /**
    * 描述：根据Id 查询
    *
    */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<Halls> getById(@PathVariable("id") Long id)throws Exception {
        Halls halls = hallsService.getById(id);
        return JsonResponse.success(halls);
    }
}

