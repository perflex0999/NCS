package com.ncs.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.api.Result;
import com.ncs.order.entity.Station;
import com.ncs.order.mapper.StationMapper;
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
@RequestMapping("/api/admin/station")
public class AdminStationController {

    private final StationMapper stationMapper;

    public AdminStationController(StationMapper stationMapper) {
        this.stationMapper = stationMapper;
    }

    @GetMapping("/list")
    public Result<List<Station>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<Station> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            qw.like(Station::getName, name);
        }
        qw.orderByDesc(Station::getId);
        return Result.ok(stationMapper.selectList(qw));
    }

    @PostMapping
    public Result<Station> create(@RequestBody Station station) {
        station.setId(null);
        if (station.getStatus() == null) {
            station.setStatus(Station.STATUS_OPEN);
        }
        stationMapper.insert(station);
        return Result.ok(station);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Station station) {
        station.setId(id);
        stationMapper.updateById(station);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stationMapper.deleteById(id);
        return Result.ok();
    }
}
