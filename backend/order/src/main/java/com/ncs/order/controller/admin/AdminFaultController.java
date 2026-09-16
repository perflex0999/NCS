package com.ncs.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.exception.BizException;
import com.ncs.common.api.Result;
import com.ncs.order.entity.Device;
import com.ncs.order.entity.Fault;
import com.ncs.order.mapper.DeviceMapper;
import com.ncs.order.mapper.FaultMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/fault")
public class AdminFaultController {

    private final FaultMapper faultMapper;
    private final DeviceMapper deviceMapper;

    public AdminFaultController(FaultMapper faultMapper, DeviceMapper deviceMapper) {
        this.faultMapper = faultMapper;
        this.deviceMapper = deviceMapper;
    }

    @GetMapping("/list")
    public Result<List<Fault>> list() {
        return Result.ok(faultMapper.selectList(
                new LambdaQueryWrapper<Fault>().orderByDesc(Fault::getFaultTime)));
    }

    @PostMapping
    public Result<Fault> create(@RequestBody Fault fault) {
        if (fault.getDeviceNo() != null && !fault.getDeviceNo().isBlank()) {
            Device device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<Device>().eq(Device::getDeviceNo, fault.getDeviceNo()));
            if (device == null) {
                throw new BizException("设备不存在");
            }
            fault.setDeviceId(device.getId());
        }
        fault.setId(null);
        if (fault.getFaultTime() == null) {
            fault.setFaultTime(LocalDateTime.now());
        }
        if (fault.getStatus() == null) {
            fault.setStatus(Fault.STATUS_PENDING);
        }
        faultMapper.insert(fault);
        return Result.ok(fault);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Fault fault) {
        fault.setId(id);
        faultMapper.updateById(fault);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        faultMapper.deleteById(id);
        return Result.ok();
    }
}
