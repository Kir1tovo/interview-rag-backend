package com.rhy.interviewprep.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
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

    private static final String SYSTEM_PROMPT = "你是一个智能助手，帮助用户进行面试准备。你可以回答关于JD解析、技能匹配、学习计划等问题。当你需要访问外部数据或执行操作时，可以使用提供的工具，所有文件查找、读取、搜索操作，根目录固定为 D:\\rhy\\rag，只允许在该文件夹内查找文件，禁止使用绝对盘符 D:\\，直接写文件名即可，工具会自动拼接基础路径，读取文件时必须填写完整文件名包含后缀，例如 hello.txt，不能只写 hello；\n" +
            "若用户只提供文件名无后缀，主动询问确认文件后缀。";

    @Autowired(required = false)
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    @Lazy
    @Bean
    public ReactAgent agentReactAgent(ChatModel chatModel) {
        var builder = ReactAgent.builder()
                .name("interview-prep-agent")
                .model(chatModel)
                .systemPrompt(SYSTEM_PROMPT)
                // 限制模型调用次数为10次，防止无限循环
                .hooks(ModelCallLimitHook.builder().runLimit(10).build())
                .saver(new MemorySaver());

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