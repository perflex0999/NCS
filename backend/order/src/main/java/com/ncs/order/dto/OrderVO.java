package com.ncs.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private String orderNo;
    private String stationName;
    private String deviceNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private BigDecimal chargedKwh;
    private BigDecimal amount;
    private BigDecimal elecPrice;
    private BigDecimal servicePrice;
    private Integer status;
    private String statusDesc;
}
