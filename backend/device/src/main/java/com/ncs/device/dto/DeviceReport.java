package com.ncs.device.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceReport {

    private String deviceNo;
    private Long stationId;
    private BigDecimal voltage;
    private BigDecimal current;
    private BigDecimal power;
    private BigDecimal energy;
    private Integer status;
    private Long timestamp;
}
