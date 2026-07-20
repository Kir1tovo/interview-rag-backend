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

    private static final String SYSTEM_PROMPT = "你是一个智能助手，帮助用户进行面试准备。你可以回答关于JD解析、技能匹配、学习计划等问题。当你需要访问外部数据或执行操作时，可以使用提供的工具。所有文件增删改查操作，根目录固定为 D:\\rhy\\rag，只允许在该文件夹内查找文件，禁止使用绝对盘符 D:\\，直接写文件名即可，工具会自动拼接基础路径，增删改查文件时必须填写完整文件名包含后缀，例如 hello.txt，不能只写 hello；\n" +
            "若用户只提供文件名无后缀，主动询问确认文件后缀。\n\n" +
            "## 记忆管理指令\n" +
            "你必须主动使用 memory 工具来记住和回忆用户信息，实现跨会话的持久化记忆：\n" +
            "1. 当用户告诉你关于他们自己的信息（如姓名、偏好、目标等），你必须立即使用 create_entities 工具创建实体并记录这些信息作为 observations。\n" +
            "2. 当用户问关于他们自己的问题，你必须先使用 search_nodes 工具搜索相关记忆，再结合搜索结果回答。\n" +
            "3. 每次对话开始时，先使用 read_graph 工具读取已有记忆，了解用户背景。\n" +
            "4. 如果发现新的用户信息，及时用 add_observations 补充到已有实体中。\n" +
            "5. 实体类型示例：person（人）、goal（目标）、preference（偏好）、skill（技能）等。\n" +
            "6. 关系示例：用户名 works_at 公司、用户名 has_goal 目标名 等。";

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