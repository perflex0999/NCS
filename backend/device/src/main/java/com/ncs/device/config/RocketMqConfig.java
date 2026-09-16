package com.ncs.device.config;

import com.ncs.device.service.DeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * RocketMQ 生产者 + 消费者
 */
@Slf4j
@Configuration
public class RocketMqConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    private final DeviceDataService deviceDataService;

    public RocketMqConfig(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Bean(destroyMethod = "shutdown")
    public DefaultMQProducer deviceDataProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("ncs-device-producer-group");
        producer.setNamesrvAddr(nameServer);
        producer.start();
        log.info("RocketMQ 生产者启动: {}", nameServer);
        return producer;
    }

    @Bean(destroyMethod = "shutdown")
    public DefaultMQPushConsumer deviceDataConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("ncs-device-consumer-group");
        consumer.setNamesrvAddr(nameServer);
        consumer.subscribe("device-status-topic", "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                deviceDataService.process(json);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        log.info("RocketMQ 消费者启动，订阅 device-status-topic");
        return consumer;
    }
}
