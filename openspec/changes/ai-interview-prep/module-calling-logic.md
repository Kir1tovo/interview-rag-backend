# 模块调用逻辑

## 1. 模块总览

本项目共 5 个业务模块，按职责分层如下：

| 模块 | 职责 | 对外依赖 |
|------|------|----------|
| user-auth | 用户注册、登录、JWT 鉴权 | 无外部业务依赖 |
| jd-parsing | JD 文本智能解析，提取结构化信息 | user-auth |
| match-analysis | 用户技能与 JD 要求对比，生成匹配报告 | user-auth、jd-parsing |
| learning-plan | 基于匹配差距生成个性化学习计划 | user-auth、match-analysis、interview-rag |
| interview-rag | 面经知识库混合检索 | user-auth |

---

## 2. 模块调用关系图

```
┌─────────────┐
│  user-auth   │  ← 基础层：所有模块依赖
└──────┬──────┘
       │ 鉴权
       ▼
┌─────────────┐
│  jd-parsing  │  ← 解析 JD 原始文本，输出结构化数据
└──────┬──────┘
       │ 读取 JD 结构化数据 (requirements_json)
       ▼
┌──────────────┐
│ match-analysis│  ← 对比用户技能与 JD 要求，输出匹配报告
└──────┬───────┘
       │ 读取匹配差距 (need_improve_json, not_known_json)
       ▼
┌──────────────┐      推荐学习资源
│ learning-plan │ ──────────────────→ ┌───────────────┐
└──────────────┘                      │ interview-rag  │
                                      └───────────────┘
```

---

## 3. 核心调用链详解

### 3.1 主流程：从 JD 解析到学习计划

这是用户最核心的使用路径，模块间存在**顺序依赖**：

```
用户操作            模块调用                          数据流向
─────────────────────────────────────────────────────────────────
1. 注册/登录    → user-auth                     → 生成 JWT Token
2. 粘贴 JD 文本 → jd-parsing                    → 调用 DeepSeek → 保存 job_description (含 requirements_json)
3. 选择 JD 分析 → match-analysis                → 读取 user_skill + job_description → 生成 match_analysis
4. 生成学习计划 → learning-plan                 → 读取 match_analysis → 调用 DeepSeek → 保存 learning_plan
5. 检索面经     → interview-rag                 → 向量+全文混合检索 → 返回面试题
```

### 3.2 模块间接口调用明细

#### user-auth → 其他模块（横切依赖）

| 调用方式 | 说明 |
|----------|------|
| JWT Token 校验 | 所有业务接口请求前，通过拦截器校验 Token，提取 userId |
| Redis 黑名单 | 注销时将 Token 加入黑名单，所有模块共享校验逻辑 |

**调用逻辑**：不是直接调用，而是通过 **JWT 拦截器** 横切注入，所有受保护的接口自动获取当前登录用户 ID。

---

#### jd-parsing → match-analysis

| 调用方 | 被调用方 | 调用方式 | 传递数据 |
|--------|----------|----------|----------|
| match-analysis | jd-parsing | 数据库读取 | `job_description.requirements_json`、`soft_skills_json` |

**调用逻辑**：
1. `MatchService` 通过 `jdMapper.selectById(jdId)` 从数据库读取 JD 记录
2. 解析 `requirements_json` 得到 JD 要求的技术栈列表 `[{skill, required}]`
3. 与 `user_skill` 表中的用户技能逐项对比

**注意**：match-analysis 不直接调用 jd-parsing 的 Service，而是通过**共享数据库表**间接获取数据。

---

#### match-analysis → learning-plan

| 调用方 | 被调用方 | 调用方式 | 传递数据 |
|--------|----------|----------|----------|
| learning-plan | match-analysis | 数据库读取 | `match_analysis.need_improve_json`、`not_known_json`、`priority_json` |

**调用逻辑**：
1. `PlanService` 通过 `matchAnalysisMapper.selectById(matchId)` 读取匹配分析结果
2. 提取"需要加强"和"完全不会"的技能列表
3. 结合优先级建议，构造 Prompt 调用 DeepSeek 生成阶段化学习计划

**注意**：同样通过**共享数据库表**获取数据，不直接调用 match-analysis 的 Service。

---

#### learning-plan → interview-rag

| 调用方 | 被调用方 | 调用方式 | 传递数据 |
|--------|----------|----------|----------|
| learning-plan | interview-rag | Service 调用 | 学习计划中的技术点关键词 |

**调用逻辑**：
1. `PlanService` 生成学习计划后，针对每个阶段的知识点
2. 调用 `RagService.search(keywords)` 检索相关面经题目
3. 将推荐的面经题目关联到学习计划中，供用户跳转学习

**注意**：这是唯一一个**直接 Service 调用**的跨模块依赖，因为需要实时检索面经数据。

---

## 4. 外部服务调用

| 模块 | 外部服务 | 调用场景 | 缓存策略 |
|------|----------|----------|----------|
| jd-parsing | DeepSeek API | 解析 JD 原始文本 | `jd:parse:{userId}:{contentHash}` TTL 7天 |
| match-analysis | DeepSeek API | 生成优先级建议 | 无（每次分析可能不同） |
| learning-plan | DeepSeek API | 生成阶段化学习计划 | 无（每次生成可能不同） |
| interview-rag | DashScope API | 生成查询向量 | 无（查询实时生成） |

---

## 5. 数据库表依赖关系

```
sys_user
  │
  ├── user_skill          ← match-analysis 读取
  │
  ├── job_description     ← jd-parsing 写入，match-analysis 读取
  │     │
  │     └── match_analysis ← match-analysis 写入，learning-plan 读取
  │           │
  │           └── learning_plan ← learning-plan 写入
  │
  └── interview_question  ← interview-rag 读写（独立，无外键依赖其他业务表）
```

---

## 6. 缓存调用逻辑

| 场景 | 缓存 Key | 调用方 | 说明 |
|------|----------|--------|------|
| JD 解析 | `jd:parse:{userId}:{contentHash}` | jd-parsing | 相同 JD 文本不重复调大模型 |
| 用户信息 | `user:info:{userId}` | user-auth | 减少数据库查询 |
| Token 黑名单 | `auth:blacklist:{token}` | user-auth | 注销后 Token 失效 |
| 热门面经 | `interview:qa:{queryHash}` | interview-rag | 高频问题答案缓存 |
| 接口限流 | `rate:limit:{userId}:{apiPath}` | 全局 | 防止滥用 |

---

## 7. 调用时序图（核心场景）

### 场景：用户完成一次完整的面试准备流程

```
用户          前端           user-auth    jd-parsing    match-analysis    learning-plan    interview-rag
 │             │               │             │               │                │               │
 │──登录────→  │──Token校验──→  │             │               │                │               │
 │             │←──Token──────  │             │               │                │               │
 │             │               │             │               │                │               │
 │──粘贴JD──→  │──解析JD────→  │             │──调DeepSeek→  │                │               │
 │             │               │             │←──结构化JSON── │                │               │
 │             │               │             │──写DB────────→│                │               │
 │             │←──解析结果──── │             │               │                │               │
 │             │               │             │               │                │               │
 │──匹配分析─→ │──请求匹配──→  │             │               │──读JD+技能──→  │               │
 │             │               │             │               │──调DeepSeek→  │               │
 │             │               │             │               │←──优先级建议── │               │
 │             │               │             │               │──写DB────────→ │               │
 │             │←──匹配报告──── │             │               │                │               │
 │             │               │             │               │                │               │
 │──生成计划─→ │──请求计划──→  │             │               │                │──读匹配报告→  │
 │             │               │             │               │                │──调DeepSeek→  │
 │             │               │             │               │                │──搜面经──────→│
 │             │               │             │               │                │←──面经推荐──── │
 │             │               │             │               │                │──写DB────────→│
 │             │←──学习计划──── │             │               │                │               │
```

---

## 8. 模块独立性说明

| 模块 | 可独立使用 | 依赖前置模块 |
|------|-----------|-------------|
| user-auth | ✅ 是 | 无 |
| jd-parsing | ❌ 否 | user-auth（需登录） |
| match-analysis | ❌ 否 | user-auth + jd-parsing（需已有 JD 数据） |
| learning-plan | ❌ 否 | user-auth + match-analysis（需已有匹配报告） |
| interview-rag | ✅ 是 | 仅需 user-auth（可独立检索面经） |

**注意**：`interview-rag` 虽然可独立使用，但与 `learning-plan` 存在协作关系——学习计划会调用面经检索来推荐学习资源。