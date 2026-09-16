package com.ncs.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.order.entity.Price;
import com.ncs.order.mapper.PriceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

/**
 * 分时段价格服务
 */
@Service
public class PriceService {

    private final PriceMapper priceMapper;

    public PriceService(PriceMapper priceMapper) {
        this.priceMapper = priceMapper;
    }

    /**
     * 查询某充电站某设备类型在「当前时刻」生效的价格
     */
    public Price getCurrentPrice(Long stationId, Integer deviceType) {
        List<Price> prices = priceMapper.selectList(new LambdaQueryWrapper<Price>()
                .eq(Price::getStationId, stationId)
                .eq(Price::getDeviceType, deviceType));
        if (prices.isEmpty()) {
            return null;
        }
        LocalTime now = LocalTime.now();
        for (Price p : prices) {
            if (inPeriod(now, p.getStartTime(), p.getEndTime())) {
                return p;
            }
        }
        // 找不到当前时段时，返回第一条作为兜底
        return prices.get(0);
    }

    private boolean inPeriod(LocalTime now, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        // 跨午夜时段（如 22:00 - 08:00）
        return !now.isBefore(start) || now.isBefore(end);
    }
}
