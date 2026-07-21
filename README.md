# AI 面试备战平台

基于 Spring AI + Vue 3 的全栈 AI 面试备战平台，集成大模型能力为求职者提供从岗位解析、技能匹配到学习规划的全链路智能辅助。

## 技术栈

**后端：** Java 21 / Spring Boot 3.3.0 / Spring AI 1.1.0 / Spring AI Alibaba 1.1.2.0 / MyBatis-Plus 3.5.7 / PostgreSQL + pgvector / Redis / Spring Security + JWT

**前端：** Vue 3.4 / Vite 5.4 / Element Plus 2.7 / Vue Router 4.3 / Axios

**AI 模型：** DeepSeek (Chat) / 智谱 AI GLM-4V (OCR) / DashScope text-embedding-v3 (Embedding)

**协议：** MCP (Model Context Protocol)

## 核心功能

### 🤖 ReAct Agent 智能助手
- 基于 Spring AI ReactAgent 实现多轮推理对话
- 集成 MCP Client（filesystem / memory）与本地 @Tool 工具
- SkillsAgentHook 动态加载 skill 文件，支持工具调用与推理轮次限制

### 🔍 RAG 面经语义检索
- DashScope text-embedding-v3 生成文本向量
- pgvector 余弦相似度检索（`<=>` 操作符），支持相似度阈值过滤
- Redis 缓存热门查询结果（TTL 2 小时），减少 Embedding 开销
- 面经详情页相似题目推荐

### 📄 OCR + LLM JD 解析
- 智谱 GLM-4V 图片 OCR 识别 → DeepSeek 结构化解析
- 自动提取公司、岗位、技术栈、软技能等结构化信息
- 图片格式校验、文本长度检查、Markdown 清理等容错机制

### 🎯 AI 人岗匹配分析
- LLM 语义匹配：精确匹配 / 子集匹配 / 超集匹配 / 相关匹配四级评分
- 置信度加权评分，解决"数据库" vs "MySQL"等模糊技能匹配问题
- 技术栈权重 70% + 软技能权重 30% 综合评分
- 技能缺口分析与学习优先级建议

### 📚 个性化学习计划
- 基于匹配分析结果，LLM 生成多阶段学习计划
- 按掌握 / 需加强 / 完全不会分类，附优先级排序与预估时长
- 支持重新生成计划

### 🔐 JWT 无状态认证
- BCrypt 密码加密 + JWT Token 认证
- Redis Token 黑名单实现安全注销
- Spring Security 过滤器链统一鉴权

## 项目结构

```
├── backend-service/               # 后端服务
│   └── src/main/java/com/rhy/interviewprep/
│       ├── config/                # 配置类（Agent、Security、CORS等）
│       ├── controller/            # REST 控制器
│       ├── service/imp/           # 业务逻辑实现
│       ├── mapper/                # MyBatis-Plus Mapper（含pgvector原生SQL）
│       ├── entity/                # 数据库实体
│       ├── dto/                   # 数据传输对象
│       ├── security/              # JWT 过滤器与认证逻辑
│       ├── tools/                 # Agent @Tool 工具类
│       ├── constants/             # Prompt 模板常量
│       └── common/                # 异常处理、错误码
├── frontend/                      # 前端应用
│   └── src/
│       ├── views/                 # 页面组件
│       │   ├── interview/         # 面经搜索、列表、详情
│       │   ├── jd/                # JD 解析、列表
│       │   ├── match/             # 人岗匹配分析
│       │   ├── plan/              # 学习计划
│       │   └── skill/             # 技能管理
│       ├── components/            # 公共组件（AgentDialog等）
│       └── utils/                 # API 请求工具
├── data/                          # MCP Memory 数据
├── uploads/                       # 上传文件存储
└── pom.xml                        # Maven 父 POM
```

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- PostgreSQL 14+（需安装 pgvector 扩展）
- Redis 6+
- Maven 3.8+

### 1. 数据库配置

安装 pgvector 扩展并创建数据库：

```sql
CREATE DATABASE interview_prep;
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. 环境变量

配置以下环境变量（或直接修改 `application.yml`）：

```bash
export DEEPSEEK_API_KEY=your_deepseek_api_key（deepseek）
export DASHSCOPE_API_KEY=your_dashscope_api_key（千问）
export ZHIPUAI_API_KEY=your_zhipuai_api_key（智谱）
```

### 3. 后端启动

```bash
cd backend-service
mvn spring-boot:run
```

后端服务启动在 `http://localhost:8080`

### 4. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端应用启动在 `http://localhost:5173`，自动代理 `/api` 请求到后端

### 5. MCP 服务（可选）

Agent 的 MCP 工具依赖 Node.js 包，首次启动时会自动通过 npx 安装：

- `@modelcontextprotocol/server-filesystem` — 文件系统操作
- `@modelcontextprotocol/server-memory` — 记忆管理

## API 概览

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | `POST /api/user/register` | 用户注册 |
| 用户 | `POST /api/user/login` | 用户登录 |
| 用户 | `POST /api/user/logout` | 用户注销 |
| 面经 | `POST /api/interview/search` | 语义检索面经 |
| 面经 | `GET /api/interview/{id}/similar` | 相似题目推荐 |
| JD | `POST /api/jd/upload` | 上传JD图片解析 |
| 匹配 | `POST /api/match/analyze` | 人岗匹配分析 |
| 学习 | `POST /api/plan/generate` | 生成学习计划 |
| 技能 | `CRUD /api/skill` | 用户技能管理 |
| Agent | `POST /api/agent/chat` | 智能助手对话 |

## 架构亮点

- **ReAct Agent**：推理-行动循环模式，Agent 自主决策调用工具，最大 5 轮推理限制
- **RAG 检索链路**：Query → Embedding → pgvector 相似度检索 → Redis 缓存 → 结果返回
- **OCR + LLM Pipeline**：GLM-4V 图片识别 → DeepSeek 结构化提取，两阶段解耦
- **四级语义匹配**：exact(100) / subset(按等级) / superset(70) / related(40) × 置信度
- **JWT 无状态认证**：BCrypt 加密 + Redis 黑名单，无服务端 Session
