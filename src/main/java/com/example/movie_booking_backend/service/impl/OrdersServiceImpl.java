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
            Seats seat = seatsMapper.selectById(seatId);
            if (seat == null || !seat.getHallId().equals(hall.getId()) || !"AVAILABLE".equals(seat.getStatus())) {
                throw new BusinessException("座位 " + (seat != null ? seat.getSeatNumber() : seatId) + " 不可用或不存在");
            }
            validSeats.add(seat);
            totalAmount = totalAmount.add(movie.getBasePrice().multiply(hall.getPriceMultiplier()).multiply(seat.getPriceMultiplier()));
        }

        // 4. 锁定座位
        UpdateWrapper<Seats> lockWrapper = new UpdateWrapper<>();
        lockWrapper.in("id", orderCreationDTO.getSeatIds())
                .eq("hall_id", hall.getId())
                .eq("status", "AVAILABLE")
                .set("status", "RESERVED");

        int lockedRows = seatsMapper.update(null, lockWrapper);
        if (lockedRows != orderCreationDTO.getSeatIds().size()) {
            // 回滚已锁定的座位
            UpdateWrapper<Seats> unlockWrapper = new UpdateWrapper<>();
            unlockWrapper.in("id", orderCreationDTO.getSeatIds())
                    .eq("status", "RESERVED")
                    .set("status", "AVAILABLE");
            seatsMapper.update(null, unlockWrapper);
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

        // 7. 创建订单项（确保不重复）
        List<OrderItems> orderItems = validSeats.stream()
                .map(seat -> {
                    OrderItems item = new OrderItems();
                    item.setOrderId(order.getId()); // 使用数据库生成的订单ID
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
    public OrderVO getOrderDetails(Long orderId, Long userId) {
        Orders order = this.getOne(new QueryWrapper<Orders>().eq("id", orderId).eq("user_id", userId));
        if (order == null) throw new BusinessException("订单不存在");

        return buildOrderVO(order);
    }

    @Override
    public Page<OrderVO> getUserOrders(Page<Orders> page, Long userId) {
        Page<Orders> orderPage = this.page(page, new QueryWrapper<Orders>().eq("user_id", userId).orderByDesc("created_at"));

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
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("只有待支付的订单才能取消");
        }

        releaseSeatsForOrder(order);

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional
    public void processSuccessfulPayment(Long orderId) {
        Orders order = this.getById(orderId);
        if (order == null || !"PENDING_PAYMENT".equals(order.getStatus())) {
            return;
        }
        // 1. 更新订单状态
        order.setStatus("CONFIRMED");
        order.setUpdatedAt(LocalDateTime.now());
        this.updateById(order);
        // 2. 更新座位状态
        List<OrderItems> items = orderItemsMapper.selectList(new QueryWrapper<OrderItems>().eq("order_id", orderId));
        List<Long> seatIds = items.stream().map(OrderItems::getSeatId).collect(Collectors.toList());
        UpdateWrapper<Seats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", seatIds).eq("status", "LOCKED").set("status", "BOOKED");
        seatsMapper.update(null, updateWrapper);
        // 3. 创建支付记录
        Payments payment = new Payments();
        payment.setOrderId(orderId);
        payment.setUserId(order.getUserId());
        payment.setPaymentAmount(order.getTotalAmount());
        payment.setPaymentMethod("CASH");
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentsMapper.insert(payment);
    }

    @Override
    @Transactional
    public void handleExpiredOrders() {
        // 找出所有已超时的待支付订单
        // 计算过期时间点：当前时间减去订单过期时间（分钟）
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(ORDER_EXPIRATION_MINUTES);
        
        List<Orders> expiredOrders = this.list(new QueryWrapper<Orders>()
                .eq("status", "PENDING")
                .le("created_at", expirationTime));

        for (Orders order : expiredOrders) {
            releaseSeatsForOrder(order);
            order.setStatus("EXPIRED");
            order.setUpdatedAt(LocalDateTime.now());
            this.updateById(order);
        }
    }

    private void releaseSeatsForOrder(Orders order) {
        List<OrderItems> items = orderItemsMapper.selectList(new QueryWrapper<OrderItems>().eq("order_id", order.getId()));
        if (!items.isEmpty()) {
            List<Long> seatIds = items.stream().map(OrderItems::getSeatId).collect(Collectors.toList());
            UpdateWrapper<Seats> unlockWrapper = new UpdateWrapper<>();
            unlockWrapper.in("id", seatIds).eq("status", "LOCKED").set("status", "AVAILABLE");
            seatsMapper.update(null, unlockWrapper);
        }
    }

    private OrderVO buildOrderVO(Orders order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        List<OrderItems> items = orderItemsMapper.selectList(new QueryWrapper<OrderItems>().eq("order_id", order.getId()));
        vo.setOrderItems(items);

        if (order.getSessionId() != null) {
            vo.setSession(movieSessionsService.getSessionDetails(order.getSessionId()));
        }

        vo.setPayment(paymentsMapper.selectOne(new QueryWrapper<Payments>().eq("order_id", order.getId())));

        return vo;
    }
}
