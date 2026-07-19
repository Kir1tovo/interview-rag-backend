package com.rhy.interviewprep.constants;

import java.util.Arrays;
import java.util.List;

public class CommonSkills {

    // ========== 技术栈 ==========

    public static final List<String> PROGRAMMING_LANGUAGES = Arrays.asList(
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#", "PHP", "Ruby", "Swift", "Kotlin"
    );

    public static final List<String> FRAMEWORKS = Arrays.asList(
            "Spring Boot", "Spring Framework", "Vue.js", "React", "Angular", "Next.js",
            "Express", "Django", "Flask", "FastAPI", "MyBatis", "Hibernate"
    );

    public static final List<String> DATABASES = Arrays.asList(
            "MySQL", "PostgreSQL", "SQLite", "Oracle", "SQL Server",
            "Redis", "MongoDB", "Elasticsearch", "Cassandra"
    );

    public static final List<String> CLOUD = Arrays.asList(
            "AWS", "阿里云", "腾讯云", "华为云", "Kubernetes", "Docker",
            "Jenkins", "Git", "CI/CD"
    );

    public static final List<String> TOOLS = Arrays.asList(
            "IntelliJ IDEA", "VS Code", "WebStorm", "Postman", "Git",
            "Docker", "Jenkins", "Jira", "Confluence"
    );

    public static final List<String> ALL_COMMON_TECH_SKILLS = Arrays.asList(
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#", "PHP", "Ruby",
            "Spring Boot", "Spring Framework", "Vue.js", "React", "Angular", "Next.js", "Express",
            "Django", "Flask", "FastAPI", "MyBatis", "Hibernate",
            "MySQL", "PostgreSQL", "SQLite", "Oracle", "SQL Server", "Redis", "MongoDB", "Elasticsearch",
            "AWS", "阿里云", "腾讯云", "华为云", "Kubernetes", "Docker", "Jenkins", "Git", "CI/CD",
            "HTML", "CSS", "Node.js", "Webpack", "Vite", "WebAssembly"
    );

    // ========== 软技能 ==========

    public static final List<String> COMMUNICATION = Arrays.asList(
            "沟通能力", "表达能力", "演讲能力", "文档写作", "需求分析"
    );

    public static final List<String> TEAMWORK = Arrays.asList(
            "团队协作", "跨部门协作", "项目管理", "时间管理", "目标导向"
    );

    public static final List<String> THINKING = Arrays.asList(
            "问题解决", "逻辑思维", "批判性思维", "创新思维", "系统性思维"
    );

    public static final List<String> LEARNING = Arrays.asList(
            "学习能力", "自驱力", "抗压能力", "适应能力", "好奇心"
    );

    public static final List<String> LEADERSHIP = Arrays.asList(
            "领导力", "决策能力", "影响力", " mentoring", "冲突解决"
    );

    public static final List<String> ALL_COMMON_SOFT_SKILLS = Arrays.asList(
            "沟通能力", "表达能力", "演讲能力", "文档写作", "需求分析",
            "团队协作", "跨部门协作", "项目管理", "时间管理", "目标导向",
            "问题解决", "逻辑思维", "批判性思维", "创新思维", "系统性思维",
            "学习能力", "自驱力", "抗压能力", "适应能力", "好奇心",
            "领导力", "决策能力", "影响力", "冲突解决"
    );

    /** @deprecated 使用 ALL_COMMON_TECH_SKILLS 代替 */
    @Deprecated
    public static final List<String> ALL_COMMON_SKILLS = ALL_COMMON_TECH_SKILLS;
}