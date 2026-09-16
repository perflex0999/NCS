package com.ncs.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncs.agent.dto.ChatRequest;
import com.ncs.agent.entity.ChatHistory;
import com.ncs.agent.mapper.ChatHistoryMapper;
import com.ncs.agent.service.AgentService;
import com.ncs.common.api.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AgentController(AgentService agentService, ChatHistoryMapper chatHistoryMapper) {
        this.agentService = agentService;
        this.chatHistoryMapper = chatHistoryMapper;
    }

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatRequest req,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        SseEmitter emitter = new SseEmitter(120_000L);
        executor.execute(() -> agentService.chat(req.getMessage(), userId, req.getScenario(), emitter));
        return emitter;
    }

    @GetMapping("/history")
    public Result<List<ChatHistory>> history(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                             @RequestParam(defaultValue = "user_assistant") String scenario) {
        List<ChatHistory> list = chatHistoryMapper.selectList(new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, userId == null ? 1L : userId)
                .eq(ChatHistory::getScenario, scenario)
                .orderByAsc(ChatHistory::getId));
        return Result.ok(list);
    }

    @DeleteMapping("/history")
    public Result<Void> clear(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                              @RequestParam(defaultValue = "user_assistant") String scenario) {
        chatHistoryMapper.delete(new LambdaQueryWrapper<ChatHistory>()
                .eq(ChatHistory::getUserId, userId == null ? 1L : userId)
                .eq(ChatHistory::getScenario, scenario));
        return Result.ok();
    }
}
