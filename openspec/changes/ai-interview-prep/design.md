## Context

本项目是一个面向在校学生和初级开发者的 AI 面试准备平台，基于 Spring Boot + Spring AI 技术栈构建。项目采用前后端分离架构，后端提供 RESTful API，前端使用 Vue 3 开发。

### 当前状态
- 项目从零开始，无历史代码
- 基础设施要求：PostgreSQL 15+（含 pgvector 扩展）、Redis 7+
- 外部依赖：DeepSeek API（对话大模型）、通义千问 DashScope API（Embedding 模型）

### 约束条件
- 后端使用 Java + Spring Boot 3.3.x + Maven 构建
- ORM 框架使用 MyBatis-Plus
- 架构模式为标准 MVC
- 向量存储使用 PostgreSQL + pgvector
- 缓存和会话存储使用 Redis
- 前端使用 Vue 3 + Element Plus

## Goals / Non-Goals

**Goals:**
- 实现完整的用户认证体系（注册、登录、JWT Token 鉴权）
- 实现 JD 智能解析，通过大模型提取结构化信息
- 实现岗位匹配度分析，生成技能差距报告
- 实现个性化学习计划生成
- 实现基于 pgvector 的面经知识库 RAG 检索系统
- 提供完整的前端用户界面
- 实现 Redis 缓存优化，减少大模型调用成本

**Non-Goals:**
- 不做分布式微服务架构，单体应用即可
- 不做移动端适配，PC 端优先
- 不做支付和商业化功能
- 不做管理员后台的复杂权限体系（简单的管理员角色即可）
- 不做知识图谱（Neo4j），第一版先不实现
- 不做 Agent 工具调用，第一版聚焦 RAG 和结构化生成

## Decisions

### 1. 技术栈选型

| 决策 | 选型 | 理由 | 备选方案 |
|------|------|------|---------|
| 后端框架 | Spring Boot 3.3.x | 稳定、生态成熟、中文资料丰富 | Spring Boot 4.x（太新，资料少）|
| AI 框架 | Spring AI 1.1.x | GA 稳定版，DeepSeek 和 DashScope 均有支持 | LangChain4j |
| 对话大模型 | DeepSeek | 中文效果好、性价比高、Spring AI 原生支持 | 通义千问、文心一言 |
| 嵌入模型 | 通义千问 Embedding（DashScope）| 中文效果好、Spring AI 集成 | BGE-M3（需本地部署）|
| 主数据库 | PostgreSQL + pgvector | 关系型 + 向量一体化，减少运维成本 | MySQL + Milvus |
| 缓存/会话 | Redis | 缓存、Session、限流多用途 | 纯内存缓存（不可持久化）|
| ORM | MyBatis-Plus 3.5.x | 国内主流、面试常问、上手快 | Spring Data JPA |
| 构建工具 | Maven | 稳定、资料多 | Gradle |
| 认证方案 | JWT + Redis（黑名单） | 无状态 + 可控的注销机制 | Session Cookie |
| 前端框架 | Vue 3 + Element Plus | 国内流行、组件库完善 | React + Ant Design |
| OCR 服务 | 百度 OCR / 阿里云 OCR | 中文识别准确率高、API 稳定、Spring Boot 集成方便 | Tesseract（开源，识别率低）|

### 2. 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层                                │
│                  Vue 3 + Element Plus                        │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐    │
│  │ 登录注册 │ │ JD解析 │ │ 匹配分析 │ │ 学习计划 │ │ 面经检索 │    │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘    │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / RESTful API
┌──────────────────────────────▼──────────────────────────────┐
│                      Spring Boot 后端                         │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controller 层  (MVC)                                 │   │
│  │  UserController / JdController / MatchController     │   │
│  │  PlanController / InterviewController                │   │
│  └───────────────────────┬──────────────────────────────┘   │
│                          │                                   │
│  ┌───────────────────────▼──────────────────────────────┐   │
│  │  Service 层                                           │   │
│  │  UserService / JdService / MatchService              │   │
│  │  PlanService / InterviewService / RagService         │   │
│  └───────┬───────────────────────────┬──────────────────┘   │
│          │                           │                      │
│  ┌───────▼───────┐          ┌────────▼────────┐             │
│  │ MyBatis-Plus   │          │   Spring AI     │             │
│  │ (数据持久化)    │          │ (AI 能力封装)    │             │
│  └───────┬───────┘          └────────┬────────┘             │
│          │                           │                      │
│  ┌───────▼───────────────────────────▼───────────────┐      │
│  │              基础设施层                               │      │
│  │  Redis (缓存/Session/限流)                          │      │
│  └────────────────────────────────────────────────────┘      │
└───────────────┬──────────────────────────────────────────────┘
                │
    ┌───────────▼───────────┐
    │   PostgreSQL          │
    │   + pgvector          │
    │   (业务数据 + 向量)    │
    └───────────────────────┘
```

### 3. 数据库设计

核心数据表：

**sys_user（用户表）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| username | varchar(50) | 用户名 |
| password | varchar(100) | 密码（BCrypt加密）|
| nickname | varchar(100) | 昵称 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

**job_description（JD 表）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| company | varchar(200) | 公司名称 |
| department | varchar(200) | 部门 |
| position | varchar(200) | 岗位名称 |
| location | varchar(100) | 工作地点 |
| education | varchar(50) | 学历要求 |
| experience | varchar(50) | 经验要求 |
| salary | varchar(100) | 薪资范围 |
| raw_text | text | 原始 JD 文本 |
| requirements_json | jsonb | 技术栈要求 JSON |
| soft_skills_json | jsonb | 软技能要求 JSON |
| responsibilities | text | 岗位职责 |
| created_at | datetime | 创建时间 |

**user_skill（用户技能表）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| skill_name | varchar(100) | 技能名称 |
| level | tinyint | 掌握程度：1-了解 2-熟悉 3-精通 |
| created_at | datetime | 创建时间 |

**match_analysis（匹配分析表）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| jd_id | bigint | JD ID |
| match_score | decimal(5,2) | 总体匹配度 |
| jd_match_score | decimal(5,2) | 技术栈匹配度 |
| soft_skill_score | decimal(5,2) | 软技能匹配度 |
| mastered_json | jsonb | 已掌握技能列表 |
| need_improve_json | jsonb | 需要加强技能列表 |
| not_known_json | jsonb | 完全不会技能列表 |
| priority_json | jsonb | 优先级建议 |
| analysis_report | text | AI分析报告 |
| created_at | datetime | 创建时间 |

**learning_plan（学习计划表）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| jd_id | bigint | JD ID |
| match_id | bigint | 匹配分析 ID |
| phases_json | jsonb | 阶段化学习计划 JSON |
| total_estimated_hours | int | 预估总时长（小时）|
| created_at | datetime | 创建时间 |

**interview_question（面经题目表）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| question | text | 题目 |
| answer | text | 参考答案 |
| analysis | text | 解析 |
| category | varchar(100) | 技术分类 |
| difficulty | tinyint | 难度：1-简单 2-中等 3-困难 |
| company | varchar(200) | 来源公司 |
| embedding | vector | 向量（pgvector）|
| created_at | datetime | 创建时间 |

### 4. RAG 检索方案

采用混合检索策略：

```
用户查询
    │
    ├── 通义 Embedding → 向量检索 (pgvector HNSW)
    │
    ├── PostgreSQL 全文检索 (tsvector + GIN 索引)
    │
    └── 结果融合 → 加权评分排序 → Top-K 返回
```

- 向量检索：余弦相似度，使用 `pgvector` 的 HNSW 索引
- 全文检索：PostgreSQL 内置 tsvector + GIN 索引
- 融合策略：向量相似度 * 0.6 + 全文检索得分 * 0.4 = 综合得分

### 5. 缓存策略（Redis）

| 缓存类型 | Key 格式 | TTL | 说明 |
|---------|---------|-----|------|
| JD 解析结果 | `jd:parse:{userId}:{contentHash}` | 7天 | 相同内容的 JD 不用重复解析 |
| 用户信息 | `user:info:{userId}` | 30分钟 | 用户基本信息缓存 |
| Token 黑名单 | `auth:blacklist:{token}` | Token 剩余过期时间 | 注销的 Token |
| 热门面经检索 | `interview:qa:{queryHash}` | 1小时 | 高频问题答案缓存 |
| 接口限流计数 | `rate:limit:{userId}:{apiPath}` | 1分钟 | 用户接口限流 |

## Risks / Trade-offs

### 风险 1：大模型 API 调用成本
- **风险**：JD 解析、匹配分析、学习计划生成都需要调用大模型，成本可能较高
- **缓解**：
  - 实现 Redis 缓存，相同内容不重复调用
  - 接口限流，防止滥用
  - 开发环境使用 Mock 模式，减少真实调用

### 风险 2：大模型解析准确率不稳定
- **风险**：JD 解析结果可能不准确，影响后续匹配分析
- **缓解**：
  - 精心设计 System Prompt 和 Few-Shot 示例
  - 输出严格 JSON 格式，使用 Spring AI Structured Output
  - 支持用户手动编辑解析结果

### 风险 3：面经数据质量
- **风险**：GitHub 开源面经质量参差不齐，答案可能不准确
- **缓解**：
  - 优先选择高质量、Star 数多的仓库
  - 数据清洗和去重
  - 标注答案来源，用户可自行判断

### 风险 4：pgvector 性能
- **风险**：数据量大时向量检索性能可能下降
- **缓解**：
  - 使用 HNSW 索引
  - 合理设置 `ef_search` 和 `ef_construction` 参数
  - 第一版数据量不大（几千到几万条），不会有性能问题

### 权衡：单体 vs 微服务
- **选择**：单体应用
- **理由**：项目规模不大，单体足够用，减少复杂度和运维成本
- **未来可扩展**：如果后续功能增加，可以再拆分微服务

### 权衡：pgvector vs 专用向量数据库
- **选择**：pgvector
- **理由**：减少一个组件，运维简单；数据量小时性能足够；和业务数据在同一个数据库，方便联表查询
- **未来可扩展**：如果数据量达到百万级以上，可以迁移到 Milvus 或 Qdrant

## Migration Plan

本项目为全新项目，无历史数据迁移问题。部署步骤：

1. 安装 PostgreSQL 15+，创建数据库并启用 pgvector 扩展
2. 安装 Redis 7+
3. 执行数据库初始化脚本（建表 + 初始数据）
4. 配置 DeepSeek API Key 和 DashScope API Key
5. 启动后端服务
6. 启动前端服务
7. 导入面经数据（调用管理接口或脚本）

## Open Questions

1. 面经数据具体从哪些 GitHub 仓库导入？需要先调研几个高质量的开源面经仓库
2. 是否需要管理员角色的后台管理界面？还是只做用户端功能
3. 学习计划中的知识点依赖关系，是手动维护还是 AI 生成？
4. 是否需要增加"错题本"功能？（用户可以收藏不会的题目）
