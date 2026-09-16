package com.ncs.order.controller;

import com.ncs.common.api.Result;
import com.ncs.order.dto.ChargeStatusVO;
import com.ncs.order.dto.OrderVO;
import com.ncs.order.dto.StartChargeRequest;
import com.ncs.order.dto.StartChargeResponse;
import com.ncs.order.service.ChargeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/charge")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @PostMapping("/start")
    public Result<StartChargeResponse> start(@RequestBody StartChargeRequest req,
                                             @RequestHeader("X-User-Id") Long userId) {
        return Result.ok(chargeService.start(userId, req));
    }

    @GetMapping("/current")
    public Result<ChargeStatusVO> current(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(chargeService.current(userId));
    }

    @PostMapping("/end")
    public Result<OrderVO> end(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(chargeService.end(userId));
    }
}
