## Why

面向求职的在校学生和初级开发者，传统的面试准备方式效率低下——需要手动整理海量面经、逐条比对岗位要求、自行规划学习路线。本项目基于 Spring AI + RAG 技术构建智能化面试准备平台，通过 AI 自动解析岗位 JD、分析技能差距、生成个性化学习计划、提供智能面经检索，帮助用户高效备考。

## What Changes

- 新建完整的 Spring Boot 后端项目，采用 MVC 架构、MyBatis-Plus ORM、PostgreSQL + pgvector 数据库、Redis 缓存
- 集成 Spring AI，对接 DeepSeek 对话大模型和通义千问 Embedding 模型
- 实现用户注册登录体系（账号密码 + JWT Token）
- 实现 JD 智能解析：用户上传 JD 图片，通过 OCR 识别提取文本后，再从文本中提取结构化信息（公司、岗位、技术栈、学历、经验等）
- 实现岗位匹配度分析：用户技能与 JD 要求对比，生成匹配度报告和优先级建议
- 实现个性化学习计划：根据技能差距生成学习路线，推荐学习资源
- 实现面经知识库（RAG）：基于 pgvector 的混合检索，支持按技术点/公司/维度检索面试题
- 新建 Vue 3 + Element Plus 前端项目，提供完整的用户交互界面

## Capabilities

### New Capabilities

- `user-auth`: 用户注册、登录、个人信息管理，账号密码认证 + JWT Token
- `jd-parsing`: JD 智能解析，从文本中提取公司、岗位、技术栈、学历、经验等结构化信息
- `match-analysis`: 岗位匹配度分析，用户技能与 JD 要求对比，生成匹配报告
- `learning-plan`: 个性化学习计划生成，根据差距推荐学习路线和资源
- `interview-rag`: 面经知识库，基于 pgvector 的混合检索，智能问答

### Modified Capabilities

## Impact

- 新建后端工程 `interview-prep-backend`（Spring Boot 3.3 + MyBatis-Plus + Maven）
- 新建前端工程 `interview-prep-frontend`（Vue 3 + Element Plus + Vite）
- 新增外部依赖：DeepSeek API、通义千问 DashScope API
- 基础设施要求：PostgreSQL 15+（含 pgvector）、Redis 7+
- 数据来源：面经数据来自 GitHub 开源面经仓库，JD 数据由用户手动录入
