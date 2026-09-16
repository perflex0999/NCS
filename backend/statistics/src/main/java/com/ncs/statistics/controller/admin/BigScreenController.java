package com.ncs.statistics.controller.admin;

import com.ncs.common.api.Result;
import com.ncs.statistics.mapper.StatsMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏数据接口（返回大屏所需格式，真实数据）
 */
@RestController
@RequestMapping("/api/bigscreen")
public class BigScreenController {

    private final StatsMapper statsMapper;

    public BigScreenController(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    @GetMapping("/data")
    public Result<Map<String, Object>> data() {
        Map<String, Object> result = new LinkedHashMap<>();

        // meta
        Map<String, Object> meta = new HashMap<>();
        meta.put("dataThrough", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("meta", meta);

        long idle = nz(statsMapper.countIdle());
        long using = nz(statsMapper.countUsing());
        long fault = nz(statsMapper.countFaultDevices());
        long total = nz(statsMapper.countDevices());
        BigDecimal revenue = statsMapper.sumRevenue() == null ? BigDecimal.ZERO : statsMapper.sumRevenue();

        // kpi
        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalOrders", statsMapper.countOrders());
        kpi.put("totalRevenue", revenue);
        kpi.put("onlineChargers", idle + using);
        kpi.put("totalChargers", total);
        kpi.put("registeredUsers", statsMapper.countUsers());
        kpi.put("todayRevenue", statsMapper.todayRevenue() == null ? BigDecimal.ZERO : statsMapper.todayRevenue());
        kpi.put("monthRevenue", revenue);
        result.put("kpi", kpi);

        // chargerStatus
        Map<String, Object> cs = new LinkedHashMap<>();
        cs.put("idle", idle);
        cs.put("using", using);
        cs.put("fault", fault);
        cs.put("total", total);
        result.put("chargerStatus", cs);

        // stations
        result.put("stations", statsMapper.listStations());

        // powerType
        Map<String, Object> pt = new LinkedHashMap<>();
        pt.put("fast", nz(statsMapper.countFast()));
        pt.put("slow", nz(statsMapper.countSlow()));
        pt.put("ultra", 0);
        result.put("powerType", pt);

        // stationRank
        result.put("stationRank", statsMapper.stationRank());

        // revenueTrend（近30日，字段转成 date/revenue/orders）
        String start = LocalDateTime.now().minusDays(30)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Map<String, Object>> trend = statsMapper.selectDailyTrend(start).stream().map(m -> {
            Map<String, Object> r = new LinkedHashMap<>();
            String date = String.valueOf(m.get("date"));
            r.put("date", date.length() >= 10 ? date.substring(5) : date);
            r.put("revenue", m.get("revenue"));
            r.put("orders", m.get("orderCount"));
            return r;
        }).toList();
        result.put("revenueTrend", trend);

        // health（在线率）
        result.put("health", total > 0 ? Math.round((1 - (double) fault / total) * 1000) / 10.0 : 100.0);

        // 无法从当前数据计算的字段置空（前端回退到静态种子数据）
        result.put("todayRevenueByStation", Collections.emptyList());
        result.put("userGrowth", Collections.emptyList());
        result.put("hourHeatmap", Collections.emptyList());
        result.put("loadForecast", Collections.emptyList());
        result.put("peakWarnings", Collections.emptyList());

        return Result.ok(result);
    }

    private long nz(Long v) {
        return v == null ? 0 : v;
    }
}
