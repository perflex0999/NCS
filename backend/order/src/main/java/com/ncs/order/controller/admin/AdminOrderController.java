package com.ncs.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.api.Result;
import com.ncs.order.entity.ChargingOrder;
import com.ncs.order.mapper.OrderMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderMapper orderMapper;

    public AdminOrderController(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @GetMapping("/list")
    public Result<List<ChargingOrder>> list(@RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Long stationId,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String endTime) {
        LambdaQueryWrapper<ChargingOrder> qw = new LambdaQueryWrapper<>();
        if (userId != null) {
            qw.eq(ChargingOrder::getUserId, userId);
        }
        if (stationId != null) {
            qw.eq(ChargingOrder::getStationId, stationId);
        }
        if (status != null) {
            qw.eq(ChargingOrder::getStatus, status);
        }
        if (startTime != null && !startTime.isBlank()) {
            qw.ge(ChargingOrder::getStartTime, parseTime(startTime, false));
        }
        if (endTime != null && !endTime.isBlank()) {
            qw.le(ChargingOrder::getStartTime, parseTime(endTime, true));
        }
        qw.orderByDesc(ChargingOrder::getStartTime);
        return Result.ok(orderMapper.selectList(qw));
    }

    private LocalDateTime parseTime(String s, boolean endOfDay) {
        String t = s.trim();
        if (t.length() == 10) {
            t = t + (endOfDay ? " 23:59:59" : " 00:00:00");
        }
        return LocalDateTime.parse(t, DTF);
    }
}
