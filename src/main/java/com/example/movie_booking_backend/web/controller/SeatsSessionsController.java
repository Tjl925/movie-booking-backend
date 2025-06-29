package com.example.movie_booking_backend.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.service.ISeatsSessionsService;
import com.example.movie_booking_backend.model.domain.SeatsSessions;


/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-29
 * @version v1.0
 */
@RestController
@RequestMapping("/api/seatsSessions")
public class SeatsSessionsController {

    private final Logger logger = LoggerFactory.getLogger( SeatsSessionsController.class );

    @Autowired
    private ISeatsSessionsService seatsSessionsService;


    /**
    * 描述：根据Id 查询
    *
    */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<SeatsSessions> getById(@PathVariable("id") Long id)throws Exception {
        SeatsSessions seatsSessions = seatsSessionsService.getById(id);
        return JsonResponse.success(seatsSessions);
    }
}

