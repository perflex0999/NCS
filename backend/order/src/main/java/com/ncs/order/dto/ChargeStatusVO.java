package com.ncs.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargeStatusVO {

    private String orderNo;
    private String deviceNo;
    private String stationName;
    private String deviceTypeDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    private Long elapsedSeconds;
    private BigDecimal chargedKwh;
    private BigDecimal amount;
}
