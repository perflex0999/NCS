package com.ncs.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_charging_order")
public class ChargingOrder {

    public static final int STATUS_CHARGING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_PAID = 2;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long stationId;
    private String deviceNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal chargedKwh;
    private BigDecimal elecPrice;
    private BigDecimal servicePrice;
    private BigDecimal amount;
    private Integer status;
}
