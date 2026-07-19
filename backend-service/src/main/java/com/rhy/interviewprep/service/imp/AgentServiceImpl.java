package com.rhy.interviewprep.service.imp;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.AgentMessageRequest;
import com.rhy.interviewprep.dto.AgentMessageResponse;
import com.rhy.interviewprep.service.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

/**
 * 智能体服务实现
 * 注入 ReactAgent 实现 ReAct（推理+行动）模式的对话功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final ReactAgent agentReactAgent;

    @Override
    public Result<AgentMessageResponse> chat(AgentMessageRequest request) {
        String userMessage = request.getMessage();
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }

        try {
            AssistantMessage response = agentReactAgent.call(userMessage);
            String reply = response.getText();

            AgentMessageResponse agentResponse = AgentMessageResponse.builder()
                    .reply(reply)
                    .build();

            log.info("ReactAgent 对话完成，用户消息长度: {}", userMessage.length());
            return Result.success(agentResponse);

        } catch (Exception e) {
            log.error("ReactAgent 对话失败", e);
            return Result.error(ErrorCode.AI_SERVICE_ERROR, "智能体对话失败: " + e.getMessage());
        }
    }
}