package com.ncs.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.order.dto.OrderVO;
import com.ncs.order.entity.ChargingOrder;
import com.ncs.order.entity.Station;
import com.ncs.order.mapper.OrderMapper;
import com.ncs.order.mapper.StationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final StationMapper stationMapper;

    public OrderService(OrderMapper orderMapper, StationMapper stationMapper) {
        this.orderMapper = orderMapper;
        this.stationMapper = stationMapper;
    }

    public List<OrderVO> myOrders(Long userId) {
        List<ChargingOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .orderByDesc(ChargingOrder::getStartTime));
        return orders.stream().map(this::toVO).toList();
    }

    private OrderVO toVO(ChargingOrder o) {
        Station station = stationMapper.selectById(o.getStationId());
        OrderVO vo = new OrderVO();
        vo.setOrderNo(o.getOrderNo());
        vo.setStationName(station == null ? "" : station.getName());
        vo.setDeviceNo(o.getDeviceNo());
        vo.setStartTime(o.getStartTime());
        vo.setEndTime(o.getEndTime());
        vo.setChargedKwh(o.getChargedKwh());
        vo.setAmount(o.getAmount());
        vo.setElecPrice(o.getElecPrice());
        vo.setServicePrice(o.getServicePrice());
        vo.setStatus(o.getStatus());
        vo.setStatusDesc(orderStatusDesc(o.getStatus()));
        return vo;
    }

    private String orderStatusDesc(Integer status) {
        return switch (status == null ? -1 : status) {
            case ChargingOrder.STATUS_CHARGING -> "充电中";
            case ChargingOrder.STATUS_FINISHED -> "已完成";
            case ChargingOrder.STATUS_PAID -> "已支付";
            default -> "未知";
        };
    }
}
