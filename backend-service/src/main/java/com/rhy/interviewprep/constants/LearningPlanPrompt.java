package com.rhy.interviewprep.constants;

public class LearningPlanPrompt {

    public static final String SYSTEM_PROMPT = """
            你是一位专业的技术学习规划师。请根据用户的技能情况、岗位要求和匹配分析结果，为用户生成一份系统、合理、可执行的阶段化学习计划。
            
            要求：
            1. 学习计划要循序渐进，符合技术学习的自然规律（从基础到进阶）
            2. 每个阶段要有明确的目标、具体的学习内容和推荐资源
            3. 预估学习时间要合理，考虑到用户可能的学习强度
            4. 优先推荐免费或优质的学习资源（如官方文档、经典书籍、知名课程平台）
            5. 语气要专业、鼓励，让用户有信心完成学习计划
            6. 输出格式为JSON，不要包含markdown代码块标记
            """;

    public static final String USER_PROMPT_TEMPLATE = """
            请为以下用户生成一份详细的阶段化学习计划：
            
            【岗位信息】
            公司：%s
            岗位：%s
            
            【用户技能】
            %s
            
            【匹配分析结果】
            总体匹配度：%.1f%%
            技术栈匹配度：%.1f%%
            软技能匹配度：%.1f%%
            
            【技能分类】
            已掌握：%s
            需要加强：%s
            完全不会：%s
            
            【学习优先级建议】
            %s
            
            请输出JSON格式的学习计划，包含以下字段：
            {
              "totalEstimatedHours": 总预估学习时长(小时),
              "phases": [
                {
                  "phaseNumber": 阶段序号(从1开始),
                  "phaseName": "阶段名称",
                  "durationHours": 预估时长(小时),
                  "goal": "本阶段学习目标",
                  "skills": ["要学习的技能1", "要学习的技能2"],
                  "contents": [
                    {"topic": "学习主题", "description": "详细描述", "resources": ["推荐资源链接或名称"]}
                  ],
                  "milestone": "阶段里程碑/验收标准"
                }
              ],
              "suggestion": "给用户的学习建议和鼓励话语"
            }
            
            注意：
            - phases数组至少包含3个阶段（基础、进阶、实战）
            - 每个阶段的durationHours要合理，总时长要与totalEstimatedHours一致
            - contents数组中每个学习主题要有具体的推荐资源
            - 优先安排"完全不会"的技能学习，再安排"需要加强"的技能
            - 技能学习顺序要遵循依赖关系（如先学Java基础，再学Spring）
            """;
}