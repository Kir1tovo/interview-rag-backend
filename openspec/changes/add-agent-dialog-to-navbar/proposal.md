## Why

为了提升用户体验，需要在前端主界面导航栏底部添加一个智能体对话框，用于实现工具调用功能。同时需要实现完整的后端逻辑，支持消息处理、大模型调用和工具执行，使用户能够便捷地与AI智能体进行对话交互。

## What Changes

- 在前端主界面导航栏（Layout.vue）底部添加智能体对话框组件
- 实现消息输入框和发送按钮
- 添加对话框展开/收起功能
- 后端创建AgentController控制器，提供对话相关API接口
- 后端创建AgentService服务层，实现消息处理和工具调用逻辑
- 集成Spring AI大模型调用能力
- 创建对话相关DTO类

## Capabilities

### New Capabilities
- `agent-dialog`: 在导航栏底部添加智能体对话框，支持消息输入和发送
- `agent-service`: 后端智能体服务，支持消息处理和工具调用

### Modified Capabilities
- 

## Impact

- 修改文件：`frontend/src/views/Layout.vue` - 添加智能体对话框组件
- 新增文件：`frontend/src/components/AgentDialog.vue` - 智能体对话框组件
- 修改文件：`frontend/src/utils/api.js` - 添加智能体对话相关API调用
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/controller/AgentController.java` - 智能体控制器
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/service/AgentService.java` - 智能体服务接口
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/service/imp/AgentServiceImpl.java` - 智能体服务实现
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/dto/AgentMessageRequest.java` - 消息请求DTO
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/dto/AgentMessageResponse.java` - 消息响应DTO
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/dto/ToolCallRequest.java` - 工具调用请求DTO
- 新增文件：`backend-service/src/main/java/com/rhy/interviewprep/dto/ToolCallResponse.java` - 工具调用响应DTO