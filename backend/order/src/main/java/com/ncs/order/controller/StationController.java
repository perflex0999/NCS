package com.ncs.order.controller;

import com.ncs.common.api.Result;
import com.ncs.order.dto.StationDetailVO;
import com.ncs.order.dto.StationVO;
import com.ncs.order.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/station")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/nearby")
    public Result<List<StationVO>> nearby(@RequestParam(required = false) BigDecimal lat,
                                          @RequestParam(required = false) BigDecimal lng,
                                          @RequestParam(required = false) Integer deviceType,
                                          @RequestParam(required = false, defaultValue = "distance") String sortBy) {
        return Result.ok(stationService.nearby(lat, lng, deviceType, sortBy));
    }

    @GetMapping("/{id}")
    public Result<StationDetailVO> detail(@PathVariable Long id) {
        return Result.ok(stationService.detail(id));
    }
}
