## ADDED Requirements

### Requirement: JD 文本解析
系统 SHALL 支持用户粘贴 JD 文本，通过 AI 自动解析并提取结构化信息。

#### Scenario: 成功解析 JD
- **WHEN** 用户粘贴有效的 JD 文本并提交解析
- **THEN** 系统调用 DeepSeek 大模型解析，返回结构化的 JD 信息并保存

#### Scenario: JD 文本过短
- **WHEN** 用户提交的 JD 文本少于 50 字
- **THEN** 系统返回提示，要求输入更完整的 JD 内容

### Requirement: 解析结果字段
系统 SHALL 从 JD 文本中提取以下字段：公司名称、部门、岗位名称、工作地点、学历要求、经验要求、技术栈要求（分为必须和加分）、软技能要求、岗位职责、薪资范围。

#### Scenario: 完整解析所有字段
- **WHEN** JD 文本包含所有相关信息
- **THEN** 系统准确提取并填充所有字段

#### Scenario: 部分字段缺失
- **WHEN** JD 文本缺少某些信息（如薪资范围）
- **THEN** 缺失字段留空或标记为"未提及"，不影响其他字段解析

### Requirement: JD 列表管理
系统 SHALL 支持用户查看、删除自己的 JD 解析记录。

#### Scenario: 查看 JD 列表
- **WHEN** 用户请求查看自己的 JD 列表
- **THEN** 系统返回该用户的所有 JD 解析记录，按时间倒序排列

#### Scenario: 删除 JD
- **WHEN** 用户删除自己的某条 JD 记录
- **THEN** 系统删除该记录及其关联的匹配分析和学习计划

### Requirement: JD 详情查看
系统 SHALL 允许用户查看某条 JD 的完整解析结果和原始文本。

#### Scenario: 查看 JD 详情
- **WHEN** 用户请求查看某条 JD 的详情
- **THEN** 系统返回结构化解析结果和原始 JD 文本

### Requirement: JD 解析缓存
系统 SHALL 对相同的 JD 文本解析结果进行缓存，避免重复调用大模型。

#### Scenario: 缓存命中
- **WHEN** 用户提交的 JD 文本与之前解析过的内容完全相同（同一用户）
- **THEN** 系统直接从缓存中返回结果，不调用大模型

#### Scenario: 缓存未命中
- **WHEN** 用户提交的 JD 文本未在缓存中
- **THEN** 系统调用大模型解析，并将结果存入缓存
