package com.ncs.station.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.common.exception.BizException;
import com.ncs.station.dto.DeviceVO;
import com.ncs.station.dto.PriceVO;
import com.ncs.station.dto.StationDetailVO;
import com.ncs.station.dto.StationVO;
import com.ncs.station.entity.Device;
import com.ncs.station.entity.Price;
import com.ncs.station.entity.Station;
import com.ncs.station.mapper.DeviceMapper;
import com.ncs.station.mapper.PriceMapper;
import com.ncs.station.mapper.StationMapper;
import com.ncs.station.util.GeoUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StationService {

    private final StationMapper stationMapper;
    private final DeviceMapper deviceMapper;
    private final PriceMapper priceMapper;

    public StationService(StationMapper stationMapper, DeviceMapper deviceMapper, PriceMapper priceMapper) {
        this.stationMapper = stationMapper;
        this.deviceMapper = deviceMapper;
        this.priceMapper = priceMapper;
    }

    public List<StationVO> nearby(BigDecimal lat, BigDecimal lng, Integer deviceType, String sortBy) {
        List<Station> stations = stationMapper.selectList(
                new LambdaQueryWrapper<Station>().eq(Station::getStatus, Station.STATUS_OPEN));
        List<StationVO> result = new ArrayList<>();
        for (Station s : stations) {
            List<Device> devices = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>().eq(Device::getStationId, s.getId()));
            int fastCount = 0;
            int slowCount = 0;
            int idleCount = 0;
            for (Device d : devices) {
                if (d.getDeviceType() == Device.TYPE_FAST) {
                    fastCount++;
                } else if (d.getDeviceType() == Device.TYPE_SLOW) {
                    slowCount++;
                }
                if (d.getStatus() == Device.STATUS_IDLE) {
                    idleCount++;
                }
            }
            if (deviceType != null) {
                if (deviceType == Device.TYPE_FAST && fastCount == 0) {
                    continue;
                }
                if (deviceType == Device.TYPE_SLOW && slowCount == 0) {
                    continue;
                }
            }

            int typeForPrice = (deviceType != null) ? deviceType
                    : (fastCount > 0 ? Device.TYPE_FAST : Device.TYPE_SLOW);
            Price price = getCurrentPrice(s.getId(), typeForPrice);

            StationVO vo = new StationVO();
            vo.setStationId(s.getId());
            vo.setName(s.getName());
            vo.setAddress(s.getAddress());
            vo.setCity(s.getCity());
            vo.setBusinessHours(s.getBusinessHours());
            vo.setFastCount(fastCount);
            vo.setSlowCount(slowCount);
            vo.setIdleCount(idleCount);
            vo.setLat(s.getLat());
            vo.setLng(s.getLng());
            if (price != null) {
                vo.setElecPrice(price.getElecPrice());
                vo.setServicePrice(price.getServicePrice());
                vo.setUnitPrice(price.getElecPrice().add(price.getServicePrice()));
            }
            if (lat != null && lng != null) {
                double km = GeoUtil.distanceKm(lat, lng, s.getLat(), s.getLng());
                vo.setDistanceKm(BigDecimal.valueOf(km).setScale(2, RoundingMode.HALF_UP));
            }
            result.add(vo);
        }

        if ("price".equalsIgnoreCase(sortBy)) {
            result.sort(Comparator.comparing(v -> v.getUnitPrice() == null ? BigDecimal.ZERO : v.getUnitPrice()));
        } else {
            result.sort(Comparator.comparing(v -> v.getDistanceKm() == null ? BigDecimal.ZERO : v.getDistanceKm()));
        }
        return result;
    }

    public StationDetailVO detail(Long stationId) {
        Station s = stationMapper.selectById(stationId);
        if (s == null) {
            throw new BizException("充电站不存在");
        }
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().eq(Device::getStationId, stationId));
        List<Price> prices = priceMapper.selectList(
                new LambdaQueryWrapper<Price>().eq(Price::getStationId, stationId));

        StationDetailVO vo = new StationDetailVO();
        vo.setStationId(s.getId());
        vo.setName(s.getName());
        vo.setAddress(s.getAddress());
        vo.setCity(s.getCity());
        vo.setBusinessHours(s.getBusinessHours());
        vo.setContact(s.getContact());
        vo.setParkingInfo(s.getParkingInfo());
        vo.setStatus(s.getStatus());
        vo.setLat(s.getLat());
        vo.setLng(s.getLng());
        vo.setDevices(devices.stream().map(this::toDeviceVO).toList());
        vo.setPrices(prices.stream().map(this::toPriceVO).toList());
        return vo;
    }

    private Price getCurrentPrice(Long stationId, Integer deviceType) {
        List<Price> prices = priceMapper.selectList(new LambdaQueryWrapper<Price>()
                .eq(Price::getStationId, stationId)
                .eq(Price::getDeviceType, deviceType));
        if (prices.isEmpty()) {
            return null;
        }
        LocalTime now = LocalTime.now();
        for (Price p : prices) {
            if (inPeriod(now, p.getStartTime(), p.getEndTime())) {
                return p;
            }
        }
        return prices.get(0);
    }

    private boolean inPeriod(LocalTime now, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end);
    }

    private DeviceVO toDeviceVO(Device d) {
        DeviceVO vo = new DeviceVO();
        vo.setDeviceId(d.getId());
        vo.setDeviceNo(d.getDeviceNo());
        vo.setDeviceType(d.getDeviceType());
        vo.setDeviceTypeDesc(d.getDeviceType() == Device.TYPE_FAST ? "快充" : "慢充");
        vo.setPowerKw(d.getPowerKw());
        vo.setStatus(d.getStatus());
        vo.setStatusDesc(deviceStatusDesc(d.getStatus()));
        return vo;
    }

    private PriceVO toPriceVO(Price p) {
        PriceVO vo = new PriceVO();
        vo.setDeviceType(p.getDeviceType());
        vo.setDeviceTypeDesc(p.getDeviceType() == Device.TYPE_FAST ? "快充" : "慢充");
        vo.setStartTime(p.getStartTime().toString());
        vo.setEndTime(p.getEndTime().toString());
        vo.setElecPrice(p.getElecPrice());
        vo.setServicePrice(p.getServicePrice());
        return vo;
    }

    private String deviceStatusDesc(Integer status) {
        return switch (status == null ? -1 : status) {
            case Device.STATUS_IDLE -> "空闲";
            case Device.STATUS_CHARGING -> "使用中";
            case Device.STATUS_FAULT -> "故障";
            case Device.STATUS_OFFLINE -> "离线";
            case Device.STATUS_MAINTENANCE -> "维修中";
            default -> "未知";
        };
    }
}
