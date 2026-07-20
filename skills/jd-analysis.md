---
name: jd-analysis
description: JD职位描述分析工作流指令，指导Agent完成JD图片解析、匹配分析、技能查询等操作
---

# JD分析工作流

当用户上传JD图片或要求分析JD时，按以下工作流执行：

1. 如果用户提供了JD图片文件路径，先调用 parseJdFromImage 工具解析图片，获取JD ID和结构化信息。
2. 如果用户想对已有JD进行匹配分析（提供了JD ID或指明了某条JD），调用 analyzeJdMatch 工具进行匹配分析。
3. 如果用户想查看自己的技能，调用 getUserSkills 工具查询。
4. 如果用户想查看历史JD列表，调用 listMyJds 工具查询。
5. 典型工作流：用户上传JD图片 → parseJdFromImage解析 → analyzeJdMatch匹配分析 → 整理报告回答用户。
6. 如果用户未录入技能，提示用户先在技能管理中录入技能，否则匹配分析结果无意义。
7. 分析完成后，根据匹配结果给出针对性的学习建议和面试准备策略。