package com.ncs.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 编排：LLM + Function Calling + 流式输出
 */
@Slf4j
@Service
public class AgentService {

    private static final String SYSTEM_PROMPT = """
            你是智能充电桩运营服务平台的 AI 智能助手。你能结合系统真实业务数据回答用户问题。
            根据用户问题选择合适的工具查询数据，再用自然、友好的中文回答。
            场景：
            1. 用户充电助手：帮用户找充电站、查当前充电费用、查历史订单
            2. 智能故障咨询：诊断充电桩故障，查设备状态/订单状态后给出建议
            3. AI 运营助手：查运营数据（用户数、订单数、收入、故障数等）
            4. AI 运营报告：基于统计数据生成运营分析报告
            要求：简洁准确，只使用工具返回的真实数据，绝不编造数据。""";

    private final LlmClient llmClient;
    private final ToolService toolService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentService(LlmClient llmClient, ToolService toolService) {
        this.llmClient = llmClient;
        this.toolService = toolService;
    }

    public void chat(String userMessage, Long userId, SseEmitter emitter) {
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(msg("system", SYSTEM_PROMPT));
            messages.add(msg("user", userMessage));

            // 第一次调用（非流式）：让模型决定是否调用工具
            JsonNode resp = llmClient.chat(messages, toolDefinitions());
            JsonNode message = resp.path("choices").path(0).path("message");
            JsonNode toolCalls = message.path("tool_calls");

            if (toolCalls.isArray() && toolCalls.size() > 0) {
                // 追加 assistant 的 tool_calls
                Map<String, Object> assistantMsg = new HashMap<>();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", null);
                assistantMsg.put("tool_calls", toolCalls);
                messages.add(assistantMsg);

                // 执行工具并追加结果
                for (JsonNode tc : toolCalls) {
                    String id = tc.path("id").asText();
                    String name = tc.path("function").path("name").asText();
                    String args = tc.path("function").path("arguments").asText("");
                    String result = toolService.execute(name, args, userId);
                    Map<String, Object> toolMsg = new HashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", id);
                    toolMsg.put("content", result);
                    messages.add(toolMsg);
                }
            }

            // 最终调用（流式）：生成回答并逐段推送
            llmClient.streamChat(messages, chunk -> {
                try {
                    emitter.send(SseEmitter.event().data(chunk));
                } catch (Exception ignored) {
                }
            });
            emitter.complete();
        } catch (Exception e) {
            log.error("Agent 处理失败", e);
            try {
                emitter.send(SseEmitter.event().data("抱歉，AI 服务暂时不可用：" + e.getMessage()));
            } catch (Exception ignored) {
            }
            emitter.complete();
        }
    }

    private Map<String, Object> msg(String role, String content) {
        Map<String, Object> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private List<Map<String, Object>> toolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(fn("search_stations", "查询充电站列表，可按设备类型和是否空闲筛选",
                Map.of(
                        "location", Map.of("type", "string", "description", "位置，如城市名（可选）"),
                        "deviceType", Map.of("type", "string", "description", "快充填 fast，慢充填 slow（可选）"),
                        "idleOnly", Map.of("type", "boolean", "description", "是否只查有空闲设备的站（可选）")
                )));
        tools.add(fn("get_current_fee", "查询当前用户正在进行的充电费用", null));
        tools.add(fn("get_last_order", "查询当前用户最近一次充电订单", null));
        tools.add(fn("get_device_status", "查询某个充电桩的状态", Map.of(
                "deviceNo", Map.of("type", "string", "description", "设备编号")
        )));
        tools.add(fn("get_order_status", "查询当前用户最近的充电订单状态（用于故障诊断）", null));
        tools.add(fn("query_stats", "查询平台运营统计数据（用户数、订单数、收入、故障数等）", null));
        return tools;
    }

    private Map<String, Object> fn(String name, String desc, Map<String, Object> properties) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        params.put("properties", properties == null ? new HashMap<>() : properties);
        Map<String, Object> f = new HashMap<>();
        f.put("name", name);
        f.put("description", desc);
        f.put("parameters", params);
        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        tool.put("function", f);
        return tool;
    }
}
