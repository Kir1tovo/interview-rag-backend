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

    /**
     * LLM技能语义匹配 —— 系统提示词
     * <p>用于判断JD技能要求与用户技能之间的语义匹配关系，
     * 解决"数据库"vs"MySQL"等模糊/上下级技能无法精确匹配的问题</p>
     */
    public static final String SKILL_MATCH_SYSTEM_PROMPT = """
            你是一位技术领域的技能分类专家。你的任务是判断JD（岗位描述）中的技能要求与用户自定义技能之间的语义匹配关系。
            
            匹配类型说明：
            - exact：完全相同或同义（如 "Java"="Java"，"数据库"="Database"）
            - subset：用户的技能是JD要求的更具体实例（如JD要求"数据库"，用户有"MySQL"；JD要求"前端框架"，用户有"Vue"）
            - superset：用户的技能比JD要求更宽泛（如JD要求"MySQL"，用户有"数据库"）
            - related：相关但不属于同一技能（如JD要求"MySQL"，用户有"PostgreSQL"；JD要求"Spring Boot"，用户有"Spring"）
            - none：无任何关联
            
            判断原则：
            1. subset类型：用户的具体技能确实属于JD要求的范畴时才判定。如"MySQL"属于"数据库"，"Vue"属于"前端框架"
            2. related类型：技能有关联但不是包含关系。如"MySQL"和"PostgreSQL"都是数据库但不是同一个
            3. 优先匹配最具体的用户技能，一个JD技能最多匹配一个用户技能
            4. confidence表示你对匹配判断的把握程度（0~1），1.0表示非常确定
            
            输出要求：
            - 严格输出JSON格式，不要包含markdown代码块标记
            - 不要输出任何额外解释文字
            """;

    /**
     * LLM技能语义匹配 —— 用户提示词模板
     * <p>参数：%1$s=JD技能列表, %2$s=用户技能列表(含等级)</p>
     */
    public static final String SKILL_MATCH_USER_PROMPT_TEMPLATE = """
            请判断以下JD技能要求与用户技能的语义匹配关系：
            
            【JD技能要求】
            %s
            
            【用户技能】
            %s
            
            请为每个JD技能找出最佳匹配的用户技能，输出JSON格式：
            {
              "matches": [
                {
                  "jdSkill": "JD中的技能名称",
                  "matchedUserSkill": "匹配到的用户技能名称（无匹配则为null）",
                  "matchType": "exact/subset/superset/related/none",
                  "confidence": 0.95
                }
              ]
            }
            """;
}