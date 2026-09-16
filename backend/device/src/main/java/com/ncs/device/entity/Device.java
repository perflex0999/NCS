package com.ncs.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceNo;
    private Long stationId;
    private Integer deviceType;
    private BigDecimal powerKw;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
