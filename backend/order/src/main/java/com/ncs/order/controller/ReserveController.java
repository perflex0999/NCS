package com.ncs.order.controller;

import com.ncs.common.api.Result;
import com.ncs.order.entity.Reservation;
import com.ncs.order.service.ReserveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reserve")
public class ReserveController {

    private final ReserveService reserveService;

    public ReserveController(ReserveService reserveService) {
        this.reserveService = reserveService;
    }

    @PostMapping
    public Result<Reservation> reserve(@RequestBody Map<String, String> body,
                                       @RequestHeader("X-User-Id") Long userId) {
        Reservation r = reserveService.reserve(userId, body.get("deviceNo"));
        return Result.ok(r);
    }
}
