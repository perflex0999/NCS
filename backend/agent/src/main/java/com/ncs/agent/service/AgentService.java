package com.ncs.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncs.agent.entity.ChatHistory;
import com.ncs.agent.mapper.ChatHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 编排：LLM + Function Calling + 流式输出 + 历史记录
 * 支持 4 个场景：用户充电助手 / 智能故障咨询 / AI运营助手 / AI运营报告
 */
@Slf4j
@Service
public class AgentService {

    private static final Map<String, String> SCENARIOS = new HashMap<>();
    static {
        SCENARIOS.put("user_assistant", """
                你是智能充电桩运营平台的「用户充电助手」。帮用户：找附近充电站、查当前充电费用、查历史充电订单。
                回答要简洁友好，基于工具返回的真实数据，绝不编造。""");
        SCENARIOS.put("fault_consult", """
                你是智能充电桩运营平台的「智能故障咨询专家」。用户在充电中遇到问题时（如桩无法启动、充电中断），
                先查设备状态和订单状态，再给出初步诊断和处理建议；解决不了就引导用户提交故障反馈。""");
        SCENARIOS.put("ops_assistant", """
                你是智能充电桩运营平台的「AI 运营助手」。帮运营人员用自然语言查询运营数据：
                用户数、订单数、总收入、设备数、故障数、充电量等。回答基于真实统计数据，条理清晰。""");
        SCENARIOS.put("ops_report", """
                你是智能充电桩运营平台的「AI 运营报告生成器」。根据统计数据自动生成一份运营分析报告，
                包含：订单情况、收入情况、用户增长、设备运行情况、故障情况。用 Markdown 结构化输出。""");
    }

    private final LlmClient llmClient;
    private final ToolService toolService;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentService(LlmClient llmClient, ToolService toolService, ChatHistoryMapper chatHistoryMapper) {
        this.llmClient = llmClient;
        this.toolService = toolService;
        this.chatHistoryMapper = chatHistoryMapper;
    }

    public void chat(String userMessage, Long userId, String scenario, SseEmitter emitter) {
        try {
            String systemPrompt = SCENARIOS.getOrDefault(scenario, SCENARIOS.get("user_assistant"));
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(msg("system", systemPrompt));
            messages.add(msg("user", userMessage));

            // 保存用户消息到历史
            saveHistory(userId, scenario, "user", userMessage);

            // 第一次调用（非流式）：判断是否调用工具
            JsonNode resp = llmClient.chat(messages, toolDefinitions());
            JsonNode message = resp.path("choices").path(0).path("message");
            JsonNode toolCalls = message.path("tool_calls");

            if (toolCalls.isArray() && toolCalls.size() > 0) {
                Map<String, Object> assistantMsg = new HashMap<>();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", null);
                assistantMsg.put("tool_calls", toolCalls);
                messages.add(assistantMsg);

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
            StringBuilder full = new StringBuilder();
            llmClient.streamChat(messages, chunk -> {
                full.append(chunk);
                try {
                    emitter.send(SseEmitter.event().data(chunk));
                } catch (Exception ignored) {
                }
            });
            emitter.complete();

            // 保存助手回复到历史
            if (full.length() > 0) {
                saveHistory(userId, scenario, "assistant", full.toString());
            }
        } catch (Exception e) {
            log.error("Agent 处理失败", e);
            try {
                emitter.send(SseEmitter.event().data("抱歉，AI 服务暂时不可用：" + e.getMessage()));
            } catch (Exception ignored) {
            }
            emitter.complete();
        }
    }

    private void saveHistory(Long userId, String scenario, String role, String content) {
        try {
            ChatHistory h = new ChatHistory();
            h.setUserId(userId == null ? 1L : userId);
            h.setScenario(scenario);
            h.setRole(role);
            h.setContent(content);
            chatHistoryMapper.insert(h);
        } catch (Exception e) {
            log.warn("保存聊天历史失败: {}", e.getMessage());
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
