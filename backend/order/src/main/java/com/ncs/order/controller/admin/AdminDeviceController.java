package com.ncs.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.api.Result;
import com.ncs.order.entity.Device;
import com.ncs.order.mapper.DeviceMapper;
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
@RequestMapping("/api/admin/device")
public class AdminDeviceController {

    private final DeviceMapper deviceMapper;

    public AdminDeviceController(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    @GetMapping("/list")
    public Result<List<Device>> list(@RequestParam(required = false) Long stationId) {
        LambdaQueryWrapper<Device> qw = new LambdaQueryWrapper<>();
        if (stationId != null) {
            qw.eq(Device::getStationId, stationId);
        }
        qw.orderByDesc(Device::getId);
        return Result.ok(deviceMapper.selectList(qw));
    }

    @PostMapping
    public Result<Device> create(@RequestBody Device device) {
        device.setId(null);
        if (device.getStatus() == null) {
            device.setStatus(Device.STATUS_IDLE);
        }
        deviceMapper.insert(device);
        return Result.ok(device);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Device device) {
        device.setId(id);
        deviceMapper.updateById(device);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceMapper.deleteById(id);
        return Result.ok();
    }
}
