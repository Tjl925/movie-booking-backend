package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.*;
import com.example.movie_booking_backend.model.domain.*;
import com.example.movie_booking_backend.model.dto.OrderCreationDTO;
import com.example.movie_booking_backend.model.vo.OrderVO;
import com.example.movie_booking_backend.service.IMovieSessionsService;
import com.example.movie_booking_backend.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private SeatsMapper seatsMapper;

    @Autowired
    private HallsMapper hallsMapper;

    @Autowired
    private MoviesMapper moviesMapper;

    @Autowired
    private MovieSessionsMapper movieSessionsMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private PaymentsMapper paymentsMapper;
    @Autowired
    private SeatsSessionsMapper seatsSessionsMapper;
    @Autowired
    private IMovieSessionsService movieSessionsService;
    @Autowired
    private OrdersMapper ordersMapper;
    private static final int ORDER_EXPIRATION_MINUTES = 15; // 订单支付超时时间（分钟）

    @Override
    @Transactional
    public OrderVO createOrder(OrderCreationDTO orderCreationDTO, Long userId) {
        // 1. 校验场次
        MovieSessions session = movieSessionsMapper.selectById(orderCreationDTO.getSessionId());
        if (session == null || session.getDeleted()) {
            throw new BusinessException("场次不存在或已删除");
        }

        // 2. 获取电影和影厅信息
        Movies movie = moviesMapper.selectById(session.getMovieId());
        Halls hall = hallsMapper.selectById(session.getHallId());

        // 3. 验证座位可用性并计算总价
        List<Seats> validSeats = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(Long seatId : orderCreationDTO.getSeatIds()) {
            // 3.1 获取座位基础信息
            Seats seat = seatsMapper.selectById(seatId);
            if (seat == null || !seat.getHallId().equals(hall.getId())) {
                throw new BusinessException("座位 " + (seat != null ? seat.getSeatNumber() : seatId) + " 不存在或不属于该影厅");
            }

            // 3.2 检查座位在该场次的状态
            SeatsSessions seatSession = seatsSessionsMapper.selectOne(
                    new QueryWrapper<SeatsSessions>()
                            .eq("seat_id", seatId)
                            .eq("session_id", session.getId())
            );

            if (seatSession == null || !"AVAILABLE".equals(seatSession.getStatus())) {
                throw new BusinessException("座位 " + seat.getSeatNumber() + " 不可用");
            }

            validSeats.add(seat);
            totalAmount = totalAmount.add(movie.getBasePrice()
                    .multiply(hall.getPriceMultiplier())
                    .multiply(seat.getPriceMultiplier()));
        }

        // 4. 锁定座位（更新SeatsSessions表的状态）
        UpdateWrapper<SeatsSessions> lockWrapper = new UpdateWrapper<>();
        lockWrapper.in("seat_id", orderCreationDTO.getSeatIds())
                .eq("session_id", session.getId())
                .eq("status", "AVAILABLE")
                .set("status", "RESERVED");

        int lockedRows = seatsSessionsMapper.update(null, lockWrapper);
        if (lockedRows != orderCreationDTO.getSeatIds().size()) {
            // 回滚已锁定的座位
            UpdateWrapper<SeatsSessions> unlockWrapper = new UpdateWrapper<>();
            unlockWrapper.in("seat_id", orderCreationDTO.getSeatIds())
                    .eq("session_id", session.getId())
                    .eq("status", "RESERVED")
                    .set("status", "AVAILABLE");
            seatsSessionsMapper.update(null, unlockWrapper);
            throw new BusinessException("座位已被预订，请刷新后重试");
        }

        // 5. 创建订单
        Orders order = new Orders();
        String orderNumber = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));

        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setSessionId(session.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setSeatNumbers(validSeats.stream().map(Seats::getSeatNumber).collect(Collectors.joining(",")));
        order.setTicketCount(validSeats.size());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 6. 保存订单并获取生成的ID
        ordersMapper.insert(order);

        // 7. 创建订单项
        List<OrderItems> orderItems = validSeats.stream()
                .map(seat -> {
                    OrderItems item = new OrderItems();
                    item.setOrderId(order.getId());
                    item.setSeatId(seat.getId());
                    item.setPrice(movie.getBasePrice()
                            .multiply(hall.getPriceMultiplier())
                            .multiply(seat.getPriceMultiplier()));
                    item.setCreatedAt(LocalDateTime.now());
                    item.setUpdatedAt(LocalDateTime.now());
                    return item;
                })
                .collect(Collectors.toList());

        // 8. 批量插入订单项
        if (!orderItems.isEmpty()) {
            orderItemsMapper.insertBatchSomeColumn(orderItems);
        }

        return getOrderDetails(order.getId(), userId);
    }

    @Override
    public Orders getOrders(Long orderId) {
        return this.getById(orderId);
    }
    
    @Override
    public Orders getOrderByOrderNumber(String orderNumber) {
        return this.getOne(new QueryWrapper<Orders>().eq("order_number", orderNumber));
    }

    @Override
    public Boolean getOrderRatedStatus(Long orderId) {
        Orders order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order.getIsRated();
    }

    @Override
    public List<OrderVO> getAllOrdersByUserId(Long userId) {
        // 1. 查询用户所有未删除的订单
        List<Orders> orders = this.list(new QueryWrapper<Orders>()
                .eq("user_id", userId)
                .eq("is_deleted", false)
                .orderByDesc("created_at"));

        // 2. 转换为OrderVO列表
        return orders.stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderDetails(Long orderId, Long userId) {
        Orders order = this.getOne(new QueryWrapper<Orders>().eq("id", orderId).eq("user_id", userId));
        if (order == null) throw new BusinessException("订单不存在");

        return buildOrderVO(order);
    }

    @Override
    public Page<OrderVO> getUserOrders(Page<Orders> page, Long userId) {
        System.out.println("查询个人订单" + userId);
        Page<Orders> orderPage = this.page(page, new QueryWrapper<Orders>().eq("user_id", userId).ne("status", "CANCELLED").eq("is_deleted", false).orderByDesc("created_at"));

        Page<OrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(orderPage, voPage);

        List<OrderVO> orderVOs = orderPage.getRecords().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
        voPage.setRecords(orderVOs);

        return voPage;
    }
    
    @Override
    public Page<OrderVO> getAllOrders(Page<Orders> page) {
        Page<Orders> orderPage = this.page(page, new QueryWrapper<Orders>().eq("is_deleted", false).orderByDesc("created_at"));

        Page<OrderVO> voPage = new Page<>();
        BeanUtils.copyProperties(orderPage, voPage);

        List<OrderVO> orderVOs = orderPage.getRecords().stream()
                .map(this::buildOrderVO)
                .collect(Collectors.toList());
        voPage.setRecords(orderVOs);

        return voPage;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Orders order = this.getOne(new QueryWrapper<Orders>().eq("id", orderId).eq("user_id", userId));
        if (order == null) throw new BusinessException("订单不存在");
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待支付的订单才能取消");
        }

        releaseSeatsForOrder(order);

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        this.updateById(order);
    }
    
    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Orders order = this.getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        
        // 如果订单状态为待支付，需要释放座位
        if ("PENDING".equals(order.getStatus())) {
            releaseSeatsForOrder(order);
        }
        
        // 逻辑删除订单
        order.setDeleted(true);
        order.setUpdatedAt(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional
    public void handleExpiredOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(ORDER_EXPIRATION_MINUTES);

        List<Orders> expiredOrders = this.list(new QueryWrapper<Orders>()
                .eq("status", "PENDING")
                .le("created_at", expirationTime));

        for (Orders order : expiredOrders) {
            releaseSeatsForOrder(order);
            order.setStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());
            this.updateById(order);
        }
    }

    private void releaseSeatsForOrder(Orders order) {
        List<OrderItems> items = orderItemsMapper.selectList(
                new QueryWrapper<OrderItems>().eq("order_id", order.getId()));

        if (!items.isEmpty()) {
            // 更新 seats_sessions 表状态
            UpdateWrapper<SeatsSessions> unlockWrapper = new UpdateWrapper<>();
            unlockWrapper.in("seat_id", items.stream().map(OrderItems::getSeatId).collect(Collectors.toList()))
                    .eq("session_id", order.getSessionId())
                    .eq("status", "RESERVED")  // 注意状态值要与创建订单时设置的一致
                    .set("status", "AVAILABLE");
            seatsSessionsMapper.update(null, unlockWrapper);
        }
    }

    private OrderVO buildOrderVO(Orders order) {
        System.out.println("订单：" + order.toString());
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setTotalAmount(order.getTotalAmount().doubleValue());

        List<OrderItems> items = orderItemsMapper.selectList(new QueryWrapper<OrderItems>().eq("order_id", order.getId()));
        vo.setOrderItems(items);

        if (order.getSessionId() != null) {
            vo.setSession(movieSessionsService.getSessionDetails(order.getSessionId()));
        }

        vo.setPayment(paymentsMapper.selectOne(new QueryWrapper<Payments>().eq("order_id", order.getId()).eq("payment_status", order.getStatus())));

        return vo;
    }

    @Override
    @Transactional
    public boolean updateOrder(Orders order) {
        // 获取原订单信息
        Orders existingOrder = this.getById(order.getId());
        if (existingOrder == null) {
            return false;
        }
        
        // 检查订单状态，只有待支付的订单才能更新为已支付
        if (!"PENDING".equals(existingOrder.getStatus())) {
            return false;
        }
        
        // 更新订单状态为已支付
        existingOrder.setStatus("PAID");
        existingOrder.setPaymentMethod(order.getPaymentMethod());
        existingOrder.setPaymentTime(LocalDateTime.now());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        
        // 更新订单
        boolean updateResult = this.updateById(existingOrder);
        
        if (updateResult) {
            // 更新座位状态为已售出
            List<OrderItems> items = orderItemsMapper.selectList(
                    new QueryWrapper<OrderItems>().eq("order_id", order.getId()));
            
            if (!items.isEmpty()) {
                // 更新 seats_sessions 表状态为 OCCUPIED
                UpdateWrapper<SeatsSessions> soldWrapper = new UpdateWrapper<>();
                soldWrapper.in("seat_id", items.stream().map(OrderItems::getSeatId).collect(Collectors.toList()))
                        .eq("session_id", existingOrder.getSessionId())
                        .eq("status", "RESERVED")  // 确保只更新之前被锁定的座位
                        .set("status", "OCCUPIED");
                seatsSessionsMapper.update(null, soldWrapper);
            }
        }
        
        return updateResult;
    }
}
