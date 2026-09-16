package com.ncs.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationVO {

    private Long stationId;
    private String name;
    private String address;
    private String city;
    private String businessHours;
    private BigDecimal distanceKm;
    private Integer fastCount;
    private Integer slowCount;
    private Integer idleCount;
    private BigDecimal elecPrice;
    private BigDecimal servicePrice;
    private BigDecimal unitPrice;
    private BigDecimal lat;
    private BigDecimal lng;
}
