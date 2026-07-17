1# OCR 文字识别模块使用说明

## 功能概述

本模块实现了基于智谱 AI GLM-4V 模型的图片文字识别（OCR）功能，支持：
- 上传图片进行 OCR 文字识别
- 图片文件格式验证（支持 JPG、PNG、WEBP）
- 图片大小限制（最大 5MB）
- Base64 编码图片识别
- 批量识别
- 服务状态检查

## 配置

### 1. 配置 API Key

在 `application.yml` 中配置智谱 AI 的 API Key：

```yaml
zhipu:
  ai:
    api-key: ${ZHIPUAI_API_KEY:your_zhipuai_api_key}
    base-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
    ocr-model: glm-4v
    timeout: 60000
```

或者在环境变量中设置：

```bash
export ZHIPUAI_API_KEY=your_actual_api_key
```

### 2. 获取智谱 AI API Key

1. 访问 [智谱 AI 开放平台](https://open.bigmodel.cn/)
2. 注册/登录账号
3. 在控制台创建 API Key
4. 将 API Key 配置到项目环境变量中

## 使用方式

### 方式一：通过 HTTP API

#### 1. OCR 识别图片

```bash
POST /api/ocr/recognize
Content-Type: multipart/form-data

file: <图片文件>
```

**响应示例：**

成功：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "text": "识别出的文字内容...",
    "success": true,
    "errorMessage": null
  }
}
```

失败：
```json
{
  "code": 400,
  "message": "图片大小不能超过 5MB",
  "data": null
}
```

#### 2. 检查服务状态

```bash
GET /api/ocr/status
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 方式二：通过服务层代码

```java
@Autowired
private OcrService ocrService;

// 识别图片文件
String text = ocrService.recognizeText(imageFile);

// 识别 Base64 编码图片
String text = ocrService.recognizeTextFromBase64(base64Image, "image/png");

// 批量识别
String[] results = ocrService.batchRecognizeText(imageFiles);

// 检查服务可用性
boolean available = ocrService.isAvailable();
```

### 方式三：使用工具类

```java
// 验证图片文件
ImageUtils.validateImage(imageFile);

// 图片转 Base64
String base64 = ImageUtils.imageToBase64(imageFile);

// Base64 转字节数组
byte[] bytes = ImageUtils.base64ToBytes(base64);
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 7003 | OCR识别失败 |
| 7004 | 图片格式或大小不符合要求 |
| 7005 | OCR服务不可用 |

## 注意事项

1. **图片格式要求**：仅支持 JPG、PNG、WEBP 格式
2. **图片大小限制**：最大 5MB
3. **API Key 配置**：确保智谱 AI 的 API Key 已正确配置
4. **网络连接**：确保服务器能够访问智谱 AI 的 API 端点

## 测试

可以使用 Postman 或 curl 进行测试：

```bash
# 测试 OCR 识别
curl -X POST http://localhost:8080/api/ocr/recognize \
  -F "file=@/path/to/your/image.png"

# 测试服务状态
curl http://localhost:8080/api/ocr/status
```

## 技术实现

本模块使用智谱 AI GLM-4V 模型进行 OCR 识别：

- 通过 HTTP API 调用智谱 AI 的 chat/completions 接口
- 使用 `glm-4v` 模型，支持图片输入
- 图片通过 Base64 编码的 data URL 格式传递
- 识别结果返回纯文本内容

## 文件结构

```
backend-service/src/main/java/com/rhy/interviewprep/
├── config/
│   ├── RestTemplateConfig.java      # RestTemplate 配置
│   └── ZhipuAiProperties.java       # 智谱 AI 配置属性
├── controller/
│   └── OcrController.java           # OCR 控制器
├── dto/
│   ├── OcrResultDTO.java            # OCR 结果 DTO
│   ├── ZhipuAiOcrRequest.java       # 智谱 AI 请求 DTO
│   ├── ZhipuAiOcrResponse.java      # 智谱 AI 响应 DTO
│   └── ZhipuAiErrorResponse.java    # 智谱 AI 错误响应 DTO
├── service/
│   └── OcrService.java              # OCR 服务
└── utils/
    └── ImageUtils.java              # 图片工具类
```

## 参考资料

- [智谱 AI 官方文档 - GLM OCR](https://docs.bigmodel.cn/cn/guide/models/vlm/glm-ocr)
- [智谱 AI 开放平台](https://open.bigmodel.cn/)