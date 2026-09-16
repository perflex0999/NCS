package com.ncs.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncs.order.entity.ChargingOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderMapper extends BaseMapper<ChargingOrder> {

    @Select("SELECT IFNULL(SUM(amount),0) FROM t_charging_order WHERE status IN (1,2)")
    BigDecimal sumAmount();

    @Select("SELECT IFNULL(SUM(charged_kwh),0) FROM t_charging_order WHERE status IN (1,2)")
    BigDecimal sumChargedKwh();

    @Select("SELECT DATE_FORMAT(start_time, '%Y-%m-%d') AS date, COUNT(*) AS orderCount, IFNULL(SUM(amount),0) AS revenue " +
            "FROM t_charging_order WHERE start_time >= #{start} AND status IN (1,2) " +
            "GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d') ORDER BY date")
    List<Map<String, Object>> selectDailyTrend(@Param("start") String start);
}
