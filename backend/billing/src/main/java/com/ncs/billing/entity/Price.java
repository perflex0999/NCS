package com.ncs.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@TableName("t_price")
public class Price {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long stationId;
    private Integer deviceType;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal elecPrice;
    private BigDecimal servicePrice;
}
