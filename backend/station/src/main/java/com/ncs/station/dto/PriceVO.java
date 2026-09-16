package com.ncs.station.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceVO {

    private Integer deviceType;
    private String deviceTypeDesc;
    private String startTime;
    private String endTime;
    private BigDecimal elecPrice;
    private BigDecimal servicePrice;
}
