package com.ncs.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_station")
public class Station {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private String city;
    private Integer status;
}
