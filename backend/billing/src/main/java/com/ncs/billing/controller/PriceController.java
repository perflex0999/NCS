package com.ncs.billing.controller;

import com.ncs.billing.service.PriceService;
import com.ncs.common.api.Result;
import com.ncs.common.dto.PriceDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing/price")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/current")
    public Result<PriceDTO> current(@RequestParam Long stationId, @RequestParam Integer deviceType) {
        return Result.ok(priceService.getCurrentPrice(stationId, deviceType));
    }
}
