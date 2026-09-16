package com.ncs.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncs.agent.config.LlmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 大模型 API 客户端（OpenAI 兼容，支持 Function Calling 与流式输出）
 */
@Slf4j
@Component
public class LlmClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LlmProperties props;

    public LlmClient(LlmProperties props) {
        this.props = props;
    }

    /**
     * 非流式调用（用于 Function Calling 判断）
     */
    public JsonNode chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", props.getModel());
            body.put("messages", messages);
            body.put("temperature", props.getTemperature());
            if (tools != null && !tools.isEmpty()) {
                body.put("tools", tools);
            }
            body.put("stream", false);

            HttpResponse<String> resp = httpClient.send(buildRequest(body), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("LLM 调用失败 status={} body={}", resp.statusCode(), resp.body());
                throw new RuntimeException("LLM 调用失败: HTTP " + resp.statusCode());
            }
            return objectMapper.readTree(resp.body());
        } catch (Exception e) {
            throw new RuntimeException("调用大模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式调用（生成最终回答，逐段回调）
     */
    public void streamChat(List<Map<String, Object>> messages, Consumer<String> onChunk) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", props.getModel());
            body.put("messages", messages);
            body.put("temperature", props.getTemperature());
            body.put("stream", true);

            httpClient.sendAsync(buildRequest(body), HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(resp -> resp.body().forEach(line -> {
                        if (!line.startsWith("data: ")) {
                            return;
                        }
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            return;
                        }
                        try {
                            JsonNode node = objectMapper.readTree(data);
                            String content = node.path("choices").path(0).path("delta").path("content").asText("");
                            if (!content.isEmpty()) {
                                onChunk.accept(content);
                            }
                        } catch (Exception e) {
                            // 忽略无法解析的行
                        }
                    }))
                    .join();
        } catch (Exception e) {
            throw new RuntimeException("流式调用大模型失败: " + e.getMessage(), e);
        }
    }

    private HttpRequest buildRequest(Map<String, Object> body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        return HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + props.getApiKey())
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
