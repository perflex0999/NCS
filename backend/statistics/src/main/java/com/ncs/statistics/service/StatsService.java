package com.ncs.statistics.service;

import com.ncs.statistics.mapper.StatsMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public Map<String, Object> overview() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userCount", statsMapper.countUsers());
        map.put("orderCount", statsMapper.countOrders());
        map.put("deviceCount", statsMapper.countDevices());
        map.put("faultCount", statsMapper.countFaults());
        map.put("chargedKwh", statsMapper.sumChargedKwh());
        map.put("revenue", statsMapper.sumRevenue());
        return map;
    }

    public List<Map<String, Object>> trend(int days) {
        String start = LocalDateTime.now().minusDays(days)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return statsMapper.selectDailyTrend(start);
    }
}
