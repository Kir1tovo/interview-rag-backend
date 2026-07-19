package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.AgentMessageRequest;
import com.rhy.interviewprep.dto.AgentMessageResponse;
import com.rhy.interviewprep.service.AgentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能体控制器
 * 提供智能体对话接口，后续可扩展工具调用
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    /**
     * 发送对话消息
     *
     * @param request 消息请求（message）
     * @return 对话响应（reply）
     */
    @PostMapping("/message")
    public Result<AgentMessageResponse> sendMessage(@RequestBody AgentMessageRequest request) {
        return agentService.chat(request);
    }
}