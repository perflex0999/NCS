package com.ncs.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ncs.agent.entity.ChargingOrder;
import com.ncs.agent.entity.Device;
import com.ncs.agent.entity.Station;
import com.ncs.agent.mapper.DeviceMapper;
import com.ncs.agent.mapper.OrderMapper;
import com.ncs.agent.mapper.StationMapper;
import com.ncs.agent.mapper.StatsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 工具集：结合真实业务数据，供 LLM Function Calling 调用
 */
@Slf4j
@Service
public class ToolService {

    private final StationMapper stationMapper;
    private final DeviceMapper deviceMapper;
    private final OrderMapper orderMapper;
    private final StatsMapper statsMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolService(StationMapper stationMapper, DeviceMapper deviceMapper,
                       OrderMapper orderMapper, StatsMapper statsMapper) {
        this.stationMapper = stationMapper;
        this.deviceMapper = deviceMapper;
        this.orderMapper = orderMapper;
        this.statsMapper = statsMapper;
    }

    public String execute(String name, String argsJson, Long userId) {
        try {
            JsonNode args = (argsJson == null || argsJson.isBlank())
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(argsJson);
            return switch (name) {
                case "search_stations" -> searchStations(args);
                case "get_current_fee" -> getCurrentFee(userId);
                case "get_last_order" -> getLastOrder(userId);
                case "get_device_status" -> getDeviceStatus(args.path("deviceNo").asText(""));
                case "get_order_status" -> getOrderStatus(userId);
                case "query_stats" -> queryStats();
                default -> "{\"error\":\"未知工具: " + name + "\"}";
            };
        } catch (Exception e) {
            log.warn("工具执行失败 {}: {}", name, e.getMessage());
            return "{\"error\":\"工具执行失败: " + e.getMessage() + "\"}";
        }
    }

    private String searchStations(JsonNode args) throws Exception {
        String deviceType = args.path("deviceType").asText("");
        boolean idleOnly = args.path("idleOnly").asBoolean(false);

        List<Station> stations = stationMapper.selectList(
                new LambdaQueryWrapper<Station>().eq(Station::getStatus, 1));
        List<Device> devices = deviceMapper.selectList(null);

        ArrayNode arr = objectMapper.createArrayNode();
        for (Station s : stations) {
            int fast = 0, slow = 0, idle = 0;
            for (Device d : devices) {
                if (!d.getStationId().equals(s.getId())) {
                    continue;
                }
                if (d.getDeviceType() == Device.TYPE_FAST) {
                    fast++;
                } else {
                    slow++;
                }
                if (d.getStatus() == Device.STATUS_IDLE) {
                    idle++;
                }
            }
            if ("fast".equalsIgnoreCase(deviceType) && fast == 0) {
                continue;
            }
            if ("slow".equalsIgnoreCase(deviceType) && slow == 0) {
                continue;
            }
            if (idleOnly && idle == 0) {
                continue;
            }
            ObjectNode o = objectMapper.createObjectNode();
            o.put("name", s.getName());
            o.put("address", s.getAddress());
            o.put("city", s.getCity());
            o.put("fastCount", fast);
            o.put("slowCount", slow);
            o.put("idleCount", idle);
            arr.add(o);
        }
        return objectMapper.writeValueAsString(arr);
    }

    private String getCurrentFee(Long userId) throws Exception {
        if (userId == null) {
            return "{\"error\":\"未登录\"}";
        }
        ChargingOrder order = orderMapper.selectOne(new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .eq(ChargingOrder::getStatus, ChargingOrder.STATUS_CHARGING)
                .orderByDesc(ChargingOrder::getStartTime)
                .last("LIMIT 1"));
        ObjectNode o = objectMapper.createObjectNode();
        if (order == null) {
            o.put("charging", false);
            o.put("message", "当前没有进行中的充电");
            return o.toString();
        }
        long minutes = Duration.between(order.getStartTime(), LocalDateTime.now()).toMinutes();
        o.put("charging", true);
        o.put("deviceNo", order.getDeviceNo());
        o.put("startTime", order.getStartTime().toString());
        o.put("elapsedMinutes", minutes);
        o.put("elecPrice", order.getElecPrice());
        o.put("servicePrice", order.getServicePrice());
        o.put("unitPrice", order.getElecPrice().add(order.getServicePrice()));
        return o.toString();
    }

    private String getLastOrder(Long userId) throws Exception {
        if (userId == null) {
            return "{\"error\":\"未登录\"}";
        }
        ChargingOrder order = orderMapper.selectOne(new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .orderByDesc(ChargingOrder::getStartTime)
                .last("LIMIT 1"));
        if (order == null) {
            return "{\"message\":\"还没有充电订单\"}";
        }
        ObjectNode o = objectMapper.createObjectNode();
        o.put("orderNo", order.getOrderNo());
        o.put("deviceNo", order.getDeviceNo());
        o.put("startTime", order.getStartTime() == null ? "" : order.getStartTime().toString());
        o.put("endTime", order.getEndTime() == null ? "" : order.getEndTime().toString());
        o.put("chargedKwh", order.getChargedKwh());
        o.put("amount", order.getAmount());
        o.put("status", orderStatusText(order.getStatus()));
        return o.toString();
    }

    private String getDeviceStatus(String deviceNo) throws Exception {
        if (deviceNo.isBlank()) {
            return "{\"error\":\"缺少设备编号\"}";
        }
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>().eq(Device::getDeviceNo, deviceNo));
        if (device == null) {
            return "{\"error\":\"设备不存在: " + deviceNo + "\"}";
        }
        ObjectNode o = objectMapper.createObjectNode();
        o.put("deviceNo", device.getDeviceNo());
        o.put("deviceType", device.getDeviceType() == Device.TYPE_FAST ? "快充" : "慢充");
        o.put("status", deviceStatusText(device.getStatus()));
        return o.toString();
    }

    private String getOrderStatus(Long userId) throws Exception {
        if (userId == null) {
            return "{\"error\":\"未登录\"}";
        }
        ChargingOrder order = orderMapper.selectOne(new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .orderByDesc(ChargingOrder::getStartTime)
                .last("LIMIT 1"));
        if (order == null) {
            return "{\"message\":\"还没有充电订单\"}";
        }
        ObjectNode o = objectMapper.createObjectNode();
        o.put("orderNo", order.getOrderNo());
        o.put("deviceNo", order.getDeviceNo());
        o.put("status", orderStatusText(order.getStatus()));
        return o.toString();
    }

    private String queryStats() throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("userCount", statsMapper.countUsers());
        o.put("orderCount", statsMapper.countOrders());
        o.put("deviceCount", statsMapper.countDevices());
        o.put("faultCount", statsMapper.countFaults());
        o.put("revenue", statsMapper.sumRevenue());
        o.put("chargedKwh", statsMapper.sumChargedKwh());
        return o.toString();
    }

    private String deviceStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case Device.STATUS_IDLE -> "空闲";
            case Device.STATUS_CHARGING -> "使用中";
            case Device.STATUS_FAULT -> "故障";
            case Device.STATUS_OFFLINE -> "离线";
            case Device.STATUS_MAINTENANCE -> "维修中";
            default -> "未知";
        };
    }

    private String orderStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case ChargingOrder.STATUS_CHARGING -> "充电中";
            case ChargingOrder.STATUS_FINISHED -> "已完成";
            case ChargingOrder.STATUS_PAID -> "已支付";
            default -> "未知";
        };
    }
}
