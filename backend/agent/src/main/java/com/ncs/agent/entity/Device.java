package com.ncs.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_device")
public class Device {

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_CHARGING = 1;
    public static final int STATUS_FAULT = 2;
    public static final int STATUS_OFFLINE = 3;
    public static final int STATUS_MAINTENANCE = 4;

    public static final int TYPE_FAST = 1;
    public static final int TYPE_SLOW = 2;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceNo;
    private Long stationId;
    private Integer deviceType;
    private Integer status;
}
