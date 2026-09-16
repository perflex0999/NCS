package com.ncs.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_device_data")
public class DeviceData {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceNo;
    private Long stationId;
    private BigDecimal voltage;
    private BigDecimal current;
    private BigDecimal power;
    private BigDecimal energy;
    private Integer status;
    private LocalDateTime reportTime;
    private LocalDateTime createTime;
}
