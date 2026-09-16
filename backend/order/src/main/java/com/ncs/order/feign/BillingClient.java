package com.ncs.order.feign;

import com.ncs.common.api.Result;
import com.ncs.common.dto.PriceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 billing 服务查询价格（Feign 服务间调用）
 */
@FeignClient(name = "ncs-billing")
public interface BillingClient {

    @GetMapping("/api/billing/price/current")
    Result<PriceDTO> getCurrentPrice(@RequestParam("stationId") Long stationId,
                                     @RequestParam("deviceType") Integer deviceType);
}
