package com.ncs.station.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StationService {

    private final StationMapper stationMapper;
    private final DeviceMapper deviceMapper;
    private final PriceMapper priceMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public StationService(StationMapper stationMapper, DeviceMapper deviceMapper,
                          PriceMapper priceMapper, StringRedisTemplate redisTemplate,
                          ObjectMapper objectMapper) {
        this.stationMapper = stationMapper;
        this.deviceMapper = deviceMapper;
        this.priceMapper = priceMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询附近充电站：先查 Redis 缓存，未命中则批量查询（消除 N+1）后回填缓存
     */
    public List<StationVO> nearby(BigDecimal lat, BigDecimal lng, Integer deviceType, String sortBy) {
        String key = "ncs:nearby:" + (deviceType == null ? "all" : deviceType) + ":" + sortBy;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<List<StationVO>>() {
                });
            }
        } catch (Exception e) {
            // 缓存异常则忽略，走数据库
        }

        List<StationVO> result = computeNearby(lat, lng, deviceType, sortBy);

        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), Duration.ofSeconds(5));
        } catch (Exception e) {
            // 缓存写入失败忽略
        }
        return result;
    }

    private List<StationVO> computeNearby(BigDecimal lat, BigDecimal lng, Integer deviceType, String sortBy) {
        List<Station> stations = stationMapper.selectList(
                new LambdaQueryWrapper<Station>().eq(Station::getStatus, Station.STATUS_OPEN));

        // 批量查询所有设备 + 价格（各 1 条 SQL），内存里按站分组，消除 N+1
        List<Device> allDevices = deviceMapper.selectList(null);
        Map<Long, List<Device>> devicesByStation = allDevices.stream()
                .collect(Collectors.groupingBy(Device::getStationId));

        List<Price> allPrices = priceMapper.selectList(null);
        Map<String, List<Price>> pricesByKey = allPrices.stream()
                .collect(Collectors.groupingBy(p -> p.getStationId() + ":" + p.getDeviceType()));

        LocalTime now = LocalTime.now();
        List<StationVO> result = new ArrayList<>();
        for (Station s : stations) {
            List<Device> devices = devicesByStation.getOrDefault(s.getId(), List.of());
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
            Price price = pickCurrentPrice(pricesByKey.get(s.getId() + ":" + typeForPrice), now);

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

    private Price pickCurrentPrice(List<Price> prices, LocalTime now) {
        if (prices == null || prices.isEmpty()) {
            return null;
        }
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
