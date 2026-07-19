## 1. 创建前端组件目录和基础文件

- [ ] 1.1 创建components目录（如果不存在）
- [ ] 1.2 创建AgentDialog.vue组件文件
- [ ] 1.3 添加组件基础结构（template、script、style）

## 2. 实现AgentDialog组件功能

- [ ] 2.1 实现对话框展开/收起切换功能
- [ ] 2.2 实现消息输入框和发送按钮

## 3. 集成到导航栏

- [ ] 3.1 修改Layout.vue，添加flex布局支持
- [ ] 3.2 在导航栏底部引入AgentDialog组件
- [ ] 3.3 调整导航栏样式，确保对话框固定在底部

## 4. 前端API调用和样式完善

- [ ] 4.1 在api.js中添加智能体对话相关API方法
- [ ] 4.2 完善组件样式，与现有界面风格保持一致
- [ ] 4.3 添加响应式设计，适配不同屏幕尺寸

## 5. 后端Controller层

- [ ] 5.1 创建AgentController控制器类
- [ ] 5.2 添加对话消息发送接口
- [ ] 5.3 添加对话历史查询接口
- [ ] 5.4 添加工具调用接口

## 6. 后端Service层

- [ ] 6.1 创建AgentService接口
- [ ] 6.2 创建AgentServiceImpl实现类
- [ ] 6.3 实现消息处理逻辑
- [ ] 6.4 实现工具调用逻辑

## 7. 后端DTO层

- [ ] 7.1 创建AgentMessageRequest请求DTO
- [ ] 7.2 创建AgentMessageResponse响应DTO
- [ ] 7.3 创建ToolCallRequest请求DTO
- [ ] 7.4 创建ToolCallResponse响应DTO

## 8. 后端配置和测试

- [ ] 8.1 添加智能体相关配置项到application.yml
- [ ] 8.2 配置Spring AI大模型调用
- [ ] 8.3 编写单元测试