package com.rhy.interviewprep.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能体 ReactAgent 配置
 * 基于 Spring AI Alibaba 的 ReactAgent 实现 ReAct（推理+行动）模式
 * 后续可通过 .tools() 方法添加工具调用
 */
@Configuration
public class AgentChatConfig {

    private static final String SYSTEM_PROMPT = "你是一个智能助手，帮助用户进行面试准备。你可以回答关于JD解析、技能匹配、学习计划等问题。";

    @Bean
    public ReactAgent agentReactAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("interview-prep-agent")
                .model(chatModel)
                .systemPrompt(SYSTEM_PROMPT)
                // 后续在此处添加工具调用配置，例如：
                // .tools(toolCallback1, toolCallback2)
                // 限制模型调用次数为10次，防止无限循环
                .hooks(ModelCallLimitHook.builder().runLimit(10).build())
                .saver(new MemorySaver())
                .build();
    }
}