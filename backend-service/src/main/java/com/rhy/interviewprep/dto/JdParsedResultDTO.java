package com.rhy.interviewprep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * JD 解析结果 DTO，对应 DeepSeek 大模型输出的 JSON Schema 结构。
 * 用于 Spring AI Structured Output 的反序列化目标。
 */
@Data
public class JdParsedResult {

    /** 公司名称 */
    private String company;

    /** 部门 */
    private String department;

    /** 岗位名称 */
    private String position;

    /** 工作地点 */
    private String location;

    /** 学历要求，如 "本科"、"硕士" 等 */
    private String education;

    /** 经验要求，如 "3-5年" 等 */
    private String experience;

    /** 薪资范围，如 "20k-40k" */
    private String salary;

    /** 技术栈要求 */
    private Requirements requirements;

    /** 软技能要求 */
    private SoftSkills softSkills;

    /** 岗位职责 */
    private String responsibilities;

    /**
     * 技术栈要求，分为必须和加分项
     */
    @Data
    public static class Requirements {

        /** 必须掌握的技术栈 */
        @JsonProperty("required")
        private List<String> required;

        /** 加分项技术栈 */
        @JsonProperty("preferred")
        private List<String> preferred;
    }

    /**
     * 软技能要求，分为必须和加分项
     */
    @Data
    public static class SoftSkills {

        /** 必须具备的软技能 */
        @JsonProperty("required")
        private List<String> required;

        /** 加分项软技能 */
        @JsonProperty("preferred")
        private List<String> preferred;
    }
}