package com.example.movie_booking_backend.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.service.IOrderItemsService;
import com.example.movie_booking_backend.model.domain.OrderItems;


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
@RequestMapping("/api/orderItems")
public class OrderItemsController {

    private final Logger logger = LoggerFactory.getLogger( OrderItemsController.class );

    @Autowired
    private IOrderItemsService orderItemsService;


    /**
     * 描述：根据Id 查询
     *
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<OrderItems> getById(@PathVariable("id") Long id)throws Exception {
        OrderItems orderItems = orderItemsService.getById(id);
        return JsonResponse.success(orderItems);
    }
}

