package com.ncs.simulator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 充电桩设备模拟器：连接 EMQX，周期性上报设备状态
 *
 * 用法（JVM 参数可覆盖）：
 *   -Dmqtt.broker=tcp://localhost:1883
 *   -Ddevice.count=10000        # 模拟设备数量
 *   -Ddevice.interval=5         # 上报间隔（秒）
 *   -Dmqtt.topic=device/status
 *
 * L3 目标：1 万台设备、每 5 秒上报一次 → 约 2000 条/秒
 */
public class DeviceSimulator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        String broker = System.getProperty("mqtt.broker", "tcp://localhost:1883");
        int deviceCount = Integer.parseInt(System.getProperty("device.count", "100"));
        int intervalSec = Integer.parseInt(System.getProperty("device.interval", "5"));
        String topic = System.getProperty("mqtt.topic", "device/status");

        MqttClient client = new MqttClient(broker, "device-simulator-" + System.currentTimeMillis(),
                new MemoryPersistence());
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        client.connect(opts);

        System.out.println("设备模拟器已连接 EMQX: " + broker);
        System.out.println("设备数=" + deviceCount + ", 间隔=" + intervalSec + "s, 约 " + (deviceCount / intervalSec) + " 条/秒");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long start = System.currentTimeMillis();
            for (int i = 1; i <= deviceCount; i++) {
                String deviceNo = "DEV-" + i;
                MqttMessage msg = new MqttMessage(buildMessage(deviceNo, i % 1000 + 1).getBytes());
                msg.setQos(1);
                try {
                    client.publish(topic, msg);
                } catch (Exception e) {
                    System.err.println("发布失败 " + deviceNo + ": " + e.getMessage());
                }
            }
            long cost = System.currentTimeMillis() - start;
            System.out.println("本轮上报 " + deviceCount + " 条，耗时 " + cost + "ms");
        }, 0, intervalSec, TimeUnit.SECONDS);
    }

    private static String buildMessage(String deviceNo, int stationId) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceNo", deviceNo);
        map.put("stationId", stationId);
        map.put("voltage", round(210 + RANDOM.nextDouble() * 20));
        map.put("current", round(RANDOM.nextDouble() * 30));
        map.put("power", round(RANDOM.nextDouble() * 60));
        map.put("energy", round(RANDOM.nextDouble() * 500));
        // 状态：95% 空闲，其余随机（充电中/故障/离线/维修中），用于演示状态变化
        map.put("status", RANDOM.nextInt(100) < 95 ? 0 : RANDOM.nextInt(4) + 1);
        map.put("timestamp", System.currentTimeMillis());
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化设备数据失败", e);
        }
    }

    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
