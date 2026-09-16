package com.ncs.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceVO {

    private Long deviceId;
    private String deviceNo;
    private Integer deviceType;
    private String deviceTypeDesc;
    private BigDecimal powerKw;
    private Integer status;
    private String statusDesc;
}
