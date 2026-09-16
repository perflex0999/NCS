package com.ncs.order.service;

import com.ncs.order.mapper.DeviceMapper;
import com.ncs.order.mapper.FaultMapper;
import com.ncs.order.mapper.OrderMapper;
import com.ncs.order.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final DeviceMapper deviceMapper;
    private final FaultMapper faultMapper;

    public StatsService(UserMapper userMapper, OrderMapper orderMapper,
                        DeviceMapper deviceMapper, FaultMapper faultMapper) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.deviceMapper = deviceMapper;
        this.faultMapper = faultMapper;
    }

    public Map<String, Object> overview() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userCount", userMapper.selectCount(null));
        map.put("orderCount", orderMapper.selectCount(null));
        map.put("deviceCount", deviceMapper.selectCount(null));
        map.put("faultCount", faultMapper.selectCount(null));
        map.put("chargedKwh", orderMapper.sumChargedKwh());
        map.put("revenue", orderMapper.sumAmount());
        return map;
    }

    public List<Map<String, Object>> trend(int days) {
        String start = LocalDateTime.now().minusDays(days)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return orderMapper.selectDailyTrend(start);
    }
}
