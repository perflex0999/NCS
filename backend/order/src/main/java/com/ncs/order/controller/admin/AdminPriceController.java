package com.ncs.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.api.Result;
import com.ncs.order.entity.Price;
import com.ncs.order.mapper.PriceMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/price")
public class AdminPriceController {

    private final PriceMapper priceMapper;

    public AdminPriceController(PriceMapper priceMapper) {
        this.priceMapper = priceMapper;
    }

    @GetMapping("/list")
    public Result<List<Price>> list(@RequestParam(required = false) Long stationId) {
        LambdaQueryWrapper<Price> qw = new LambdaQueryWrapper<>();
        if (stationId != null) {
            qw.eq(Price::getStationId, stationId);
        }
        qw.orderByAsc(Price::getStationId).orderByAsc(Price::getDeviceType).orderByAsc(Price::getStartTime);
        return Result.ok(priceMapper.selectList(qw));
    }

    @PostMapping
    public Result<Price> create(@RequestBody Price price) {
        price.setId(null);
        priceMapper.insert(price);
        return Result.ok(price);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Price price) {
        price.setId(id);
        priceMapper.updateById(price);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        priceMapper.deleteById(id);
        return Result.ok();
    }
}
