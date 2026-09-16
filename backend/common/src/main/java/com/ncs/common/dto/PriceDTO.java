package com.ncs.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 价格信息 DTO（billing 服务提供，order/station 通过 Feign 消费）
 */
@Data
public class PriceDTO implements Serializable {

    private Long stationId;
    private Integer deviceType;
    private BigDecimal elecPrice;
    private BigDecimal servicePrice;
}
