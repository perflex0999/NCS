package com.ncs.agent.controller;

import com.ncs.agent.dto.ChatRequest;
import com.ncs.agent.service.AgentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatRequest req,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        SseEmitter emitter = new SseEmitter(120_000L);
        executor.execute(() -> agentService.chat(req.getMessage(), userId, emitter));
        return emitter;
    }
}
