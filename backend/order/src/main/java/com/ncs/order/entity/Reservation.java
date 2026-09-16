package com.ncs.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_reservation")
public class Reservation {

    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_USED = 1;
    public static final int STATUS_CANCELLED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceNo;
    private Long userId;
    private LocalDateTime reserveTime;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime createTime;
}
