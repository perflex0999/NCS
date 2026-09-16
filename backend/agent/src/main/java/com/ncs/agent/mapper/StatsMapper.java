package com.ncs.agent.mapper;

import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

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
}
