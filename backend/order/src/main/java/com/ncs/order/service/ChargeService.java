package com.ncs.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ncs.common.api.Result;
import com.ncs.common.dto.PriceDTO;
import com.ncs.common.exception.BizException;
import com.ncs.order.dto.ChargeStatusVO;
import com.ncs.order.dto.OrderVO;
import com.ncs.order.dto.StartChargeRequest;
import com.ncs.order.dto.StartChargeResponse;
import com.ncs.order.entity.ChargingOrder;
import com.ncs.order.entity.Device;
import com.ncs.order.entity.Station;
import com.ncs.order.feign.BillingClient;
import com.ncs.order.mapper.DeviceMapper;
import com.ncs.order.mapper.OrderMapper;
import com.ncs.order.mapper.StationMapper;
import com.ncs.order.util.RedisKeys;
import com.ncs.order.util.RedisLock;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 充电核心业务：开始 / 查询 / 结束充电
 */
@Service
public class ChargeService {

    private final RedisLock redisLock;
    private final DeviceMapper deviceMapper;
    private final OrderMapper orderMapper;
    private final StationMapper stationMapper;
    private final BillingClient billingClient;

    public ChargeService(RedisLock redisLock, DeviceMapper deviceMapper, OrderMapper orderMapper,
                         StationMapper stationMapper, BillingClient billingClient) {
        this.redisLock = redisLock;
        this.deviceMapper = deviceMapper;
        this.orderMapper = orderMapper;
        this.stationMapper = stationMapper;
        this.billingClient = billingClient;
    }

    public StartChargeResponse start(Long userId, StartChargeRequest req) {
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>().eq(Device::getDeviceNo, req.getDeviceNo()));
        if (device == null) {
            throw new BizException("设备不存在");
        }

        String lockKey = RedisKeys.DEVICE_LOCK_PREFIX + req.getDeviceNo();
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisLock.tryLock(lockKey, lockValue, 10);
        if (!locked) {
            throw new BizException("系统繁忙，请稍后再试");
        }
        try {
            // 数据库层 CAS：仅当设备为空闲时才置为使用中，从根源上防止同一设备被重复充电
            int updated = deviceMapper.update(null, new LambdaUpdateWrapper<Device>()
                    .set(Device::getStatus, Device.STATUS_CHARGING)
                    .eq(Device::getId, device.getId())
                    .eq(Device::getStatus, Device.STATUS_IDLE));
            if (updated == 0) {
                throw new BizException("设备非空闲状态，无法开始充电");
            }

            // Feign 调用 billing 服务查询当前价格
            Result<PriceDTO> priceResult = billingClient.getCurrentPrice(device.getStationId(), device.getDeviceType());
            PriceDTO price = priceResult == null ? null : priceResult.getData();
            if (price == null) {
                throw new BizException("该设备未配置价格，无法充电");
            }

            ChargingOrder order = new ChargingOrder();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setStationId(device.getStationId());
            order.setDeviceId(device.getId());
            order.setDeviceNo(device.getDeviceNo());
            order.setStartTime(LocalDateTime.now());
            order.setElecPrice(price.getElecPrice());
            order.setServicePrice(price.getServicePrice());
            order.setStatus(ChargingOrder.STATUS_CHARGING);
            orderMapper.insert(order);

            StartChargeResponse resp = new StartChargeResponse();
            resp.setOrderNo(order.getOrderNo());
            resp.setDeviceNo(device.getDeviceNo());
            resp.setStartTime(order.getStartTime());
            resp.setElecPrice(order.getElecPrice());
            resp.setServicePrice(order.getServicePrice());
            return resp;
        } finally {
            redisLock.unlock(lockKey, lockValue);
        }
    }

    public ChargeStatusVO current(Long userId) {
        ChargingOrder order = findActiveOrder(userId);
        if (order == null) {
            throw new BizException("当前没有进行中的充电");
        }
        Device device = deviceMapper.selectById(order.getDeviceId());
        Station station = stationMapper.selectById(order.getStationId());

        long elapsedSeconds = Duration.between(order.getStartTime(), LocalDateTime.now()).getSeconds();
        BigDecimal chargedKwh = computeChargedKwh(device.getPowerKw(), elapsedSeconds);
        BigDecimal amount = chargedKwh.multiply(order.getElecPrice().add(order.getServicePrice()));

        ChargeStatusVO vo = new ChargeStatusVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setDeviceNo(order.getDeviceNo());
        vo.setStationName(station == null ? "" : station.getName());
        vo.setDeviceTypeDesc(device.getDeviceType() == Device.TYPE_FAST ? "快充" : "慢充");
        vo.setStartTime(order.getStartTime());
        vo.setElapsedSeconds(elapsedSeconds);
        vo.setChargedKwh(chargedKwh.setScale(2, RoundingMode.HALF_UP));
        vo.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    public OrderVO end(Long userId) {
        ChargingOrder order = findActiveOrder(userId);
        if (order == null) {
            throw new BizException("当前没有进行中的充电订单");
        }
        Device device = deviceMapper.selectById(order.getDeviceId());
        Station station = stationMapper.selectById(order.getStationId());

        LocalDateTime endTime = LocalDateTime.now();
        long elapsedSeconds = Duration.between(order.getStartTime(), endTime).getSeconds();
        BigDecimal chargedKwh = computeChargedKwh(device.getPowerKw(), elapsedSeconds);
        BigDecimal unitPrice = order.getElecPrice().add(order.getServicePrice());
        BigDecimal amount = chargedKwh.multiply(unitPrice);

        order.setEndTime(endTime);
        order.setChargedKwh(chargedKwh.setScale(2, RoundingMode.HALF_UP));
        order.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        order.setStatus(ChargingOrder.STATUS_FINISHED);
        orderMapper.updateById(order);

        deviceMapper.update(null, new LambdaUpdateWrapper<Device>()
                .set(Device::getStatus, Device.STATUS_IDLE)
                .eq(Device::getId, device.getId()));

        OrderVO vo = new OrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setStationName(station == null ? "" : station.getName());
        vo.setDeviceNo(order.getDeviceNo());
        vo.setStartTime(order.getStartTime());
        vo.setEndTime(endTime);
        vo.setChargedKwh(order.getChargedKwh());
        vo.setAmount(order.getAmount());
        vo.setElecPrice(order.getElecPrice());
        vo.setServicePrice(order.getServicePrice());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc("已完成");
        return vo;
    }

    private ChargingOrder findActiveOrder(Long userId) {
        return orderMapper.selectOne(new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .eq(ChargingOrder::getStatus, ChargingOrder.STATUS_CHARGING)
                .orderByDesc(ChargingOrder::getStartTime)
                .last("LIMIT 1"));
    }

    private BigDecimal computeChargedKwh(BigDecimal powerKw, long elapsedSeconds) {
        if (powerKw == null) {
            return BigDecimal.ZERO;
        }
        return powerKw.multiply(BigDecimal.valueOf(elapsedSeconds))
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private String generateOrderNo() {
        return "CD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + (int) (Math.random() * 1000);
    }
}
