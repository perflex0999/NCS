package com.ncs.device.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncs.device.dto.DeviceReport;
import com.ncs.device.entity.Device;
import com.ncs.device.entity.DeviceData;
import com.ncs.device.mapper.DeviceDataMapper;
import com.ncs.device.mapper.DeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 设备数据处理：更新设备状态 + 写原始上报数据
 *
 * 关键：这条链路完全独立，绝不写订单库（t_charging_order）
 */
@Slf4j
@Service
public class DeviceDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeviceMapper deviceMapper;
    private final DeviceDataMapper deviceDataMapper;

    public DeviceDataService(DeviceMapper deviceMapper, DeviceDataMapper deviceDataMapper) {
        this.deviceMapper = deviceMapper;
        this.deviceDataMapper = deviceDataMapper;
    }

    public void process(String json) {
        try {
            DeviceReport report = objectMapper.readValue(json, DeviceReport.class);

            // 1. 异步更新设备状态
            deviceMapper.update(null, new LambdaUpdateWrapper<Device>()
                    .set(Device::getStatus, report.getStatus())
                    .eq(Device::getDeviceNo, report.getDeviceNo()));

            // 2. 写原始上报数据（时序）
            DeviceData data = new DeviceData();
            data.setDeviceNo(report.getDeviceNo());
            data.setStationId(report.getStationId());
            data.setVoltage(report.getVoltage());
            data.setCurrent(report.getCurrent());
            data.setPower(report.getPower());
            data.setEnergy(report.getEnergy());
            data.setStatus(report.getStatus());
            data.setReportTime(LocalDateTime.now());
            deviceDataMapper.insert(data);
        } catch (Exception e) {
            log.warn("处理设备上报数据失败: {}", e.getMessage());
        }
    }
}
