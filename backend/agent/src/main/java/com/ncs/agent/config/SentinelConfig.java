package com.ncs.agent.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流：Agent 服务独立低配额（L3 目标 200 QPS，Agent 单独限流不影响核心充电）
 */
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("/api/agent/chat");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(200);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
}
