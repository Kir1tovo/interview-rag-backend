package com.rhy.interviewprep.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.filesystem.FileSystemSkillRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import com.rhy.interviewprep.tools.AgentTools;
import com.rhy.interviewprep.tools.JdAnalysisTools;
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
 * 集成 SkillsAgentHook，让 Agent 可以通过 read_skill 工具动态加载 .md skill 文件
 */
@Slf4j
@Configuration
public class AgentChatConfig {

    @Autowired(required = false)
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    @Autowired
    private AgentTools agentTools;

    @Autowired
    private JdAnalysisTools jdAnalysisTools;

    /**
     * 构建文件系统 Skill 注册器，扫描项目根目录 skills/ 下的 .md 文件
     * skills 目录位于项目根目录 D:/rhy/rag/skills/，方便直接编辑，修改后无需重新编译
     */
    private FileSystemSkillRegistry buildSkillRegistry() {
        String skillsPath = "D:/rhy/rag/skills";
        log.info("Skill 目录路径: {}", skillsPath);
        return FileSystemSkillRegistry.builder()
                .projectSkillsDirectory(skillsPath)
                .autoLoad(true)
                .build();
    }

    @Lazy
    @Bean
    public ReactAgent agentReactAgent(ChatModel chatModel) {
        // 构建 Skill 注册器和 Hook
        FileSystemSkillRegistry skillRegistry = buildSkillRegistry();
        SkillsAgentHook skillsAgentHook = SkillsAgentHook.builder()
                .skillRegistry(skillRegistry)
                .autoReload(false)
                .build();

        log.info("已注册 {} 个 Skill: {}", skillRegistry.size(),
                skillRegistry.listAll().stream()
                        .map(s -> s.getName() + " - " + s.getDescription())
                        .toList());

        var builder = ReactAgent.builder()
                .name("interview-prep-agent")
                .model(chatModel)
                .systemPrompt(AgentPromptConstants.SYSTEM_PROMPT)
                // 注册 Hooks：模型调用限制 + Skill 读取能力
                .hooks(
                        ModelCallLimitHook.builder().runLimit(5).build(),
                        skillsAgentHook
                )
                .saver(new MemorySaver());

        // 注册本地 @Tool 工具
        ToolCallback[] localTools = MethodToolCallbackProvider.builder()
                .toolObjects(agentTools, jdAnalysisTools)
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