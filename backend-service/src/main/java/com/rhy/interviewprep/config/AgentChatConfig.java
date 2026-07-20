package com.rhy.interviewprep.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import com.rhy.interviewprep.tools.AgentTools;
import com.rhy.interviewprep.constants.AgentPromptConstants;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 智能体 ReactAgent 配置
 * 基于 Spring AI Alibaba 的 ReactAgent 实现 ReAct（推理+行动）模式
 * 集成 MCP Client 工具调用，让 Agent 可以调用外部 MCP 服务器提供的工具
 */
@Slf4j
@Configuration
public class AgentChatConfig {

    @Autowired(required = false)
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    @Autowired
    private AgentTools agentTools;

    @Lazy
    @Bean
    public ReactAgent agentReactAgent(ChatModel chatModel) {
        var builder = ReactAgent.builder()
                .name("interview-prep-agent")
                .model(chatModel)
                .systemPrompt(AgentPromptConstants.SYSTEM_PROMPT)
                // 限制模型调用次数为5次，防止无限循环
                .hooks(ModelCallLimitHook.builder().runLimit(5).build())
                .saver(new MemorySaver());

        // 注册本地 @Tool 工具
        ToolCallback[] localTools = MethodToolCallbackProvider.builder()
                .toolObjects(agentTools)
                .build()
                .getToolCallbacks();
        if (localTools != null && localTools.length > 0) {
            builder.tools(localTools);
            log.info("已注册 {} 个本地工具到 ReactAgent", localTools.length);
            for (ToolCallback tool : localTools) {
                log.info("  - 本地工具: {}", tool.getToolDefinition().name());
            }
        }

        // 注册 MCP 工具回调
        if (mcpToolCallbackProvider != null) {
            ToolCallback[] toolCallbacks = mcpToolCallbackProvider.getToolCallbacks();
            if (toolCallbacks != null && toolCallbacks.length > 0) {
                builder.tools(toolCallbacks);
                log.info("已注册 {} 个 MCP 工具到 ReactAgent", toolCallbacks.length);
                for (ToolCallback tool : toolCallbacks) {
                    log.info("  - MCP 工具: {}", tool.getToolDefinition().name());
                }
            } else {
                log.warn("MCP 工具回调提供者已注入，但未发现可用工具。MCP 服务器可能尚未完成连接。");
            }
        } else {
            log.warn("未发现 MCP 工具回调提供者，Agent 将仅使用基础对话能力。请检查 MCP Client 配置是否正确。");
        }

        return builder.build();
    }
}