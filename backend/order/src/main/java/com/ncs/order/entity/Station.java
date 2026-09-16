package com.ncs.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_station")
public class Station {

    public static final int STATUS_CLOSED = 0;
    public static final int STATUS_OPEN = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private String city;
    private BigDecimal lat;
    private BigDecimal lng;
    private String businessHours;
    private String contact;
    private String parkingInfo;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
