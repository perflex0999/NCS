package com.ncs.order.controller;

import com.ncs.common.api.Result;
import com.ncs.order.dto.ChargeStatusVO;
import com.ncs.order.dto.OrderVO;
import com.ncs.order.dto.StartChargeRequest;
import com.ncs.order.dto.StartChargeResponse;
import com.ncs.order.service.ChargeService;
import com.ncs.order.web.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Result<StartChargeResponse> start(@RequestBody StartChargeRequest req) {
        return Result.ok(chargeService.start(UserContext.get(), req));
    }

    @GetMapping("/current")
    public Result<ChargeStatusVO> current() {
        return Result.ok(chargeService.current(UserContext.get()));
    }

    @PostMapping("/end")
    public Result<OrderVO> end() {
        return Result.ok(chargeService.end(UserContext.get()));
    }
}
