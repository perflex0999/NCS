package com.ncs.agent.dto;

import lombok.Data;

@Data
public class ChatRequest {

    private String message;
    /** 场景：user_assistant 用户充电助手 / fault_consult 智能故障咨询 / ops_assistant AI运营助手 / ops_report AI运营报告 */
    private String scenario = "user_assistant";
}
