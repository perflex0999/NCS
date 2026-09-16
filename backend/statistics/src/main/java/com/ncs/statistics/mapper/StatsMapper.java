package com.ncs.statistics.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计查询（直接聚合共享库数据）
 */
public interface StatsMapper {

    @Select("SELECT COUNT(*) FROM t_user")
    Long countUsers();

    @Select("SELECT COUNT(*) FROM t_charging_order")
    Long countOrders();

    @Select("SELECT COUNT(*) FROM t_device")
    Long countDevices();

    @Select("SELECT COUNT(*) FROM t_fault")
    Long countFaults();

    @Select("SELECT IFNULL(SUM(amount),0) FROM t_charging_order WHERE status IN (1,2)")
    BigDecimal sumRevenue();

    @Select("SELECT IFNULL(SUM(charged_kwh),0) FROM t_charging_order WHERE status IN (1,2)")
    BigDecimal sumChargedKwh();

    @Select("SELECT DATE_FORMAT(start_time, '%Y-%m-%d') AS date, COUNT(*) AS orderCount, IFNULL(SUM(amount),0) AS revenue " +
            "FROM t_charging_order WHERE start_time >= #{start} AND status IN (1,2) " +
            "GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d') ORDER BY date")
    List<Map<String, Object>> selectDailyTrend(@Param("start") String start);

    @Select("SELECT COUNT(*) FROM t_device WHERE status = 0")
    Long countIdle();

    @Select("SELECT COUNT(*) FROM t_device WHERE status = 1")
    Long countUsing();

    @Select("SELECT COUNT(*) FROM t_device WHERE status = 2")
    Long countFaultDevices();

    @Select("SELECT COUNT(*) FROM t_device WHERE device_type = 1")
    Long countFast();

    @Select("SELECT COUNT(*) FROM t_device WHERE device_type = 2")
    Long countSlow();

    @Select("SELECT s.id, s.name, s.address, s.lat AS latitude, s.lng AS longitude FROM t_station s WHERE s.status = 1")
    List<Map<String, Object>> listStations();

    @Select("SELECT s.name AS name, IFNULL(SUM(o.charged_kwh),0) AS energy, IFNULL(SUM(o.amount),0) AS revenue " +
            "FROM t_station s LEFT JOIN t_charging_order o ON s.id = o.station_id " +
            "GROUP BY s.id, s.name ORDER BY energy DESC")
    List<Map<String, Object>> stationRank();

    @Select("SELECT IFNULL(SUM(amount),0) FROM t_charging_order WHERE DATE(start_time) = CURDATE() AND status IN (1,2)")
    BigDecimal todayRevenue();
}
