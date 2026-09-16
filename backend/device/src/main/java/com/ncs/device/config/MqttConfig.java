package com.ncs.device.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT 订阅（EMQX）：收到设备上报后转发到 RocketMQ
 */
@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker:tcp://localhost:1883}")
    private String broker;

    @Value("${mqtt.topic:device/status}")
    private String topic;

    private final DefaultMQProducer producer;
    private MqttClient mqttClient;

    public MqttConfig(DefaultMQProducer producer) {
        this.producer = producer;
    }

    @PostConstruct
    public void start() throws Exception {
        mqttClient = new MqttClient(broker, "device-ingestion-" + System.currentTimeMillis(), new MemoryPersistence());
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        mqttClient.connect(opts);
        mqttClient.subscribe(topic, 1);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT 连接断开: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String t, MqttMessage message) {
                try {
                    // 转发到 RocketMQ，异步解耦，避免设备数据阻塞下游
                    Message mqMsg = new Message("device-status-topic", message.getPayload());
                    producer.send(mqMsg);
                } catch (Exception e) {
                    log.warn("转发到 RocketMQ 失败: {}", e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
        log.info("MQTT 订阅启动: {} topic={}", broker, topic);
    }

    @PreDestroy
    public void stop() throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
    }
}
