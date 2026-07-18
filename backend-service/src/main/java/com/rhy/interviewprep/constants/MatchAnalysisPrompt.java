package com.rhy.interviewprep.constants;

public class MatchAnalysisPrompt {

    public static final String SYSTEM_PROMPT = """
            你是一位专业的职业规划师和技术面试官。请根据用户的技能情况和岗位要求，生成一份详细的匹配度分析报告和学习建议。
            
            要求：
            1. 分析要客观、专业、有建设性
            2. 学习优先级建议要基于技能在岗位中的重要性和学习难度
            3. 语气要友好、鼓励，不要让用户感到压力
            4. 输出格式为JSON，不要包含markdown代码块标记
            """;

    public static final String USER_PROMPT_TEMPLATE = """
            请分析以下用户技能与岗位要求的匹配情况：
            
            【岗位信息】
            公司：%s
            岗位：%s
            
            【JD技术栈要求】
            必须掌握：%s
            加分技能：%s
            
            【JD软技能要求】
            必须具备：%s
            加分项：%s
            
            【用户技能】
            %s
            
            【匹配结果】
            技术栈匹配度：%.1f分
            软技能匹配度：%.1f分
            总体匹配度：%.1f分
            
            【技能分类】
            已掌握：%s
            需要加强：%s
            完全不会：%s
            
            请输出JSON格式的分析报告，包含以下字段：
            {
              "analysis": "详细的匹配度分析报告，包括用户优势、劣势和改进建议",
              "prioritySuggestions": [
                {"skill": "技能名称", "priority": 优先级数字(1最高), "reason": "推荐理由"}
              ]
            }
            """;
}