package com.ncs.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型 API 配置（OpenAI 兼容格式，可切换 DeepSeek/通义等）
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /** API 地址，如 https://api.deepseek.com */
    private String baseUrl = "https://api.deepseek.com";

    /** API Key（通过环境变量 LLM_API_KEY 注入，不写死在代码里） */
    private String apiKey = "";

    /** 模型名，如 deepseek-chat */
    private String model = "deepseek-chat";

    /** 温度 */
    private double temperature = 0.7;
}
