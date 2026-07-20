package com.rhy.interviewprep.tools;

import com.rhy.interviewprep.dto.MatchResultDTO;
import com.rhy.interviewprep.entity.JobDescription;
import com.rhy.interviewprep.entity.UserSkill;
import com.rhy.interviewprep.security.SecurityUtils;
import com.rhy.interviewprep.service.JdService;
import com.rhy.interviewprep.service.MatchAnalysisService;
import com.rhy.interviewprep.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JD 分析工具集
 * 提供 JD 解析、匹配分析、技能查询等 Agent Tool
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JdAnalysisTools {

    private final JdService jdService;
    private final MatchAnalysisService matchAnalysisService;
    private final UserSkillService userSkillService;

    @Tool(description = "从JD图片文件解析职位描述。读取指定路径的JD图片，通过OCR识别文本并解析为结构化的职位信息（公司、岗位、技术要求等），" +
            "解析结果会保存到数据库并返回JD ID。当用户上传了JD图片并要求分析时使用此工具。")
    public String parseJdFromImage(
            @ToolParam(description = "JD图片文件的完整路径") String filePath
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "错误：未获取到当前登录用户信息，请先登录。";
        }

        log.info("Agent调用JD图片解析: filePath={}, userId={}", filePath, userId);

        try {
            JobDescription jd = jdService.parseFromFilePath(filePath, userId);
            StringBuilder sb = new StringBuilder();
            sb.append("JD解析成功！解析结果如下：\n\n");
            sb.append(String.format("JD ID: %d\n", jd.getId()));
            sb.append(String.format("公司: %s\n", jd.getCompany() != null ? jd.getCompany() : "未知"));
            sb.append(String.format("岗位: %s\n", jd.getPosition() != null ? jd.getPosition() : "未知"));
            sb.append(String.format("部门: %s\n", jd.getDepartment() != null ? jd.getDepartment() : "未知"));
            sb.append(String.format("地点: %s\n", jd.getLocation() != null ? jd.getLocation() : "未知"));
            sb.append(String.format("学历要求: %s\n", jd.getEducation() != null ? jd.getEducation() : "未知"));
            sb.append(String.format("经验要求: %s\n", jd.getExperience() != null ? jd.getExperience() : "未知"));
            sb.append(String.format("薪资: %s\n", jd.getSalary() != null ? jd.getSalary() : "未知"));
            sb.append(String.format("\n岗位职责:\n%s\n", jd.getResponsibilities() != null ? jd.getResponsibilities() : "无"));
            sb.append(String.format("\n技术要求:\n%s\n", jd.getRequirementsJson() != null ? jd.getRequirementsJson() : "无"));
            sb.append(String.format("\n软技能要求:\n%s\n", jd.getSoftSkillsJson() != null ? jd.getSoftSkillsJson() : "无"));
            sb.append(String.format("\n请记住JD ID为 %d，后续分析匹配时需要使用。", jd.getId()));
            return sb.toString();
        } catch (Exception e) {
            log.error("JD图片解析失败", e);
            return "JD图片解析失败: " + e.getMessage();
        }
    }

    @Tool(description = "分析JD与用户技能的匹配度。根据JD ID和当前登录用户的技术技能，生成详细的匹配分析报告，" +
            "包括总分、技术匹配详情、掌握/待提升/未知技能列表及优先学习建议。当JD已解析完成后使用此工具进行匹配分析。")
    public String analyzeJdMatch(
            @ToolParam(description = "JD的ID，由parseJdFromImage返回") Long jdId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "错误：未获取到当前登录用户信息，请先登录。";
        }

        log.info("Agent调用JD匹配分析: jdId={}, userId={}", jdId, userId);

        try {
            MatchResultDTO result = matchAnalysisService.analyze(userId, jdId);

            StringBuilder sb = new StringBuilder();
            sb.append("=== JD匹配分析报告 ===\n\n");
            sb.append(String.format("综合匹配分数: %.1f/100\n", result.getTotalScore()));
            sb.append(String.format("技术技能分数: %.1f/100\n", result.getTechScore()));
            sb.append(String.format("软技能分数: %.1f/100\n\n", result.getSoftSkillScore()));

            // 技术匹配详情
            if (result.getTechMatches() != null && !result.getTechMatches().isEmpty()) {
                sb.append("【技术技能匹配详情】\n");
                for (MatchResultDTO.SkillMatchDetail detail : result.getTechMatches()) {
                    sb.append(String.format("  - %s: JD要求%s / 用户%s | 匹配状态: %s | 得分: %d\n",
                            detail.getSkillName(),
                            detail.getJdLevel() != null ? detail.getJdLevel() : "未指定",
                            detail.getUserLevel() != null ? detail.getUserLevel() : "未填写",
                            detail.getMatchStatus(),
                            detail.getScore()));
                }
                sb.append("\n");
            }

            // 掌握的技能
            if (result.getMasteredSkills() != null && !result.getMasteredSkills().isEmpty()) {
                sb.append("【已掌握的技能】: " + String.join("、", result.getMasteredSkills()) + "\n\n");
            }

            // 待提升的技能
            if (result.getNeedImproveSkills() != null && !result.getNeedImproveSkills().isEmpty()) {
                sb.append("【待提升的技能】: " + String.join("、", result.getNeedImproveSkills()) + "\n\n");
            }

            // 未知技能
            if (result.getNotKnownSkills() != null && !result.getNotKnownSkills().isEmpty()) {
                sb.append("【未掌握的技能】: " + String.join("、", result.getNotKnownSkills()) + "\n\n");
            }

            // 优先学习建议
            if (result.getPriorityItems() != null && !result.getPriorityItems().isEmpty()) {
                sb.append("【优先学习建议】\n");
                for (MatchResultDTO.PriorityItem item : result.getPriorityItems()) {
                    sb.append(String.format("  %d. %s (优先级: %d) - %s\n",
                            result.getPriorityItems().indexOf(item) + 1,
                            item.getSkillName(),
                            item.getPriority(),
                            item.getReason()));
                }
                sb.append("\n");
            }

            // 分析报告
            if (result.getAnalysisReport() != null) {
                sb.append("【综合分析】\n").append(result.getAnalysisReport());
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("JD匹配分析失败", e);
            return "JD匹配分析失败: " + e.getMessage();
        }
    }

    @Tool(description = "查询当前登录用户的所有技能。返回用户已录入的技术技能列表，包含技能名称和熟练度等级。" +
            "当需要了解用户技能背景时使用此工具。")
    public String getUserSkills() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "错误：未获取到当前登录用户信息，请先登录。";
        }

        log.info("Agent调用查询用户技能: userId={}", userId);

        List<UserSkill> skills = userSkillService.listSkills(userId);
        if (skills.isEmpty()) {
            return "当前用户尚未录入任何技能。建议先录入技能后再进行JD匹配分析。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("当前用户共有 %d 项技能：\n\n", skills.size()));
        for (UserSkill skill : skills) {
            sb.append(String.format("  - %s (熟练度: %s)\n",
                    skill.getSkillName(),
                    skill.getLevel() != null ? skillLevelLabel(skill.getLevel()) : "未指定"));
        }
        return sb.toString();
    }

    @Tool(description = "查询当前用户已解析的JD列表。返回用户所有已保存的JD记录，包含JD ID、公司名、岗位名。" +
            "当用户想查看历史JD或对已有JD进行匹配分析时使用此工具。")
    public String listMyJds(
            @ToolParam(description = "页码，从1开始，默认1") int page,
            @ToolParam(description = "每页条数，默认10") int size
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "错误：未获取到当前登录用户信息，请先登录。";
        }

        log.info("Agent调用查询用户JD列表: userId={}, page={}, size={}", userId, page, size);

        int p = Math.max(page, 1);
        int s = Math.max(Math.min(size, 20), 1);

        var result = jdService.listByUserId(userId, p, s);
        if (result.getRecords().isEmpty()) {
            return "当前用户暂无已解析的JD记录。请先上传JD图片进行解析。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 条JD记录（第%d页，共%d页）：\n\n",
                result.getTotal(), result.getCurrent(), result.getPages()));

        for (JobDescription jd : result.getRecords()) {
            sb.append(String.format("【ID:%d】%s - %s\n", jd.getId(),
                    jd.getCompany() != null ? jd.getCompany() : "未知公司",
                    jd.getPosition() != null ? jd.getPosition() : "未知岗位"));
            sb.append(String.format("  创建时间: %s\n", jd.getCreatedAt()));
            sb.append("\n");
        }

        return sb.toString();
    }

    private String skillLevelLabel(Integer level) {
        if (level == null) return "未指定";
        return switch (level) {
            case 1 -> "了解";
            case 2 -> "熟悉";
            case 3 -> "掌握";
            case 4 -> "精通";
            default -> "未指定";
        };
    }
}