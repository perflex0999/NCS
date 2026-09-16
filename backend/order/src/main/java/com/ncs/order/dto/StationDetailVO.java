package com.ncs.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StationDetailVO {

    private Long stationId;
    private String name;
    private String address;
    private String city;
    private String businessHours;
    private String contact;
    private String parkingInfo;
    private Integer status;
    private BigDecimal lat;
    private BigDecimal lng;
    private List<DeviceVO> devices;
    private List<PriceVO> prices;
}
