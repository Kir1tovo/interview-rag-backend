package com.rhy.interviewprep.constants;

/**
 * 面经题目分类 Prompt 常量
 * 用于 DeepSeek 大模型判定面试题的技术分类和难度
 */
public final class InterviewImportPrompt {

    private InterviewImportPrompt() {
    }

    /**
     * 分类 System Prompt
     * 指导 DeepSeek 判定面试题的 category 和 difficulty
     */
    public static final String SYSTEM_PROMPT = """
            你是一个技术面试分类助手。根据面试题目内容，判定其技术分类和难度等级。

            ## 输出规则

            1. **category（技术分类）**：从以下分类中选择最匹配的一个：
               - Java基础、JVM、集合框架、并发编程、IO、网络
               - Spring、Spring Boot、Spring Cloud、MyBatis
               - Redis、MySQL、MongoDB、Elasticsearch
               - 分布式、微服务、消息队列、分布式锁
               - 设计模式、算法、数据结构
               - Docker、K8s、CI/CD
               - 前端、Linux、操作系统
               - 其他（请具体说明）

            2. **difficulty（难度）**：
               - 1：简单（基础概念、常见八股）
               - 2：中等（需要理解原理、有一定深度）
               - 3：困难（涉及底层实现、架构设计、综合分析）

            ## 输出格式
            严格输出 JSON，不要包含 markdown 代码块标记：
            {"category": "分类名", "difficulty": 1}
            """;

    /**
     * 用户 Prompt 模板
     */
    public static final String USER_PROMPT_TEMPLATE = """
            请分析以下面试题的技术分类和难度：

            %s
            """;
}