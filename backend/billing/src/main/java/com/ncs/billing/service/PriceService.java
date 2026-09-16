package com.ncs.billing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.billing.entity.Price;
import com.ncs.billing.mapper.PriceMapper;
import com.ncs.common.dto.PriceDTO;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class PriceService {

    private final PriceMapper priceMapper;

    public PriceService(PriceMapper priceMapper) {
        this.priceMapper = priceMapper;
    }

    /**
     * 查询当前时刻生效的价格（供 order 服务通过 Feign 调用计价）
     */
    public PriceDTO getCurrentPrice(Long stationId, Integer deviceType) {
        List<Price> prices = priceMapper.selectList(new LambdaQueryWrapper<Price>()
                .eq(Price::getStationId, stationId)
                .eq(Price::getDeviceType, deviceType));
        if (prices.isEmpty()) {
            return null;
        }
        LocalTime now = LocalTime.now();
        for (Price p : prices) {
            if (inPeriod(now, p.getStartTime(), p.getEndTime())) {
                return toDTO(p);
            }
        }
        return toDTO(prices.get(0));
    }

    private PriceDTO toDTO(Price p) {
        PriceDTO dto = new PriceDTO();
        dto.setStationId(p.getStationId());
        dto.setDeviceType(p.getDeviceType());
        dto.setElecPrice(p.getElecPrice());
        dto.setServicePrice(p.getServicePrice());
        return dto;
    }

    private boolean inPeriod(LocalTime now, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end);
    }
}
