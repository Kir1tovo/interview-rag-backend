<template>
  <div class="jd-parse-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <el-icon><Upload /></el-icon>
          <span>上传 JD 图片</span>
        </div>
      </template>

      <el-upload
        ref="uploadRef"
        class="jd-uploader"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :before-upload="beforeUpload"
        accept="image/jpeg,image/png,image/bmp,application/pdf"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将 JD 图片拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 JPG / PNG / BMP / PDF 格式，文件大小不超过 10MB
          </div>
        </template>
      </el-upload>

      <div class="upload-actions">
        <el-button
          type="primary"
          :loading="parsing"
          :disabled="!selectedFile"
          @click="handleParse"
        >
          <el-icon v-if="!parsing"><MagicStick /></el-icon>
          {{ parsing ? '解析中...' : '开始解析' }}
        </el-button>
      </div>
    </el-card>

    <!-- 解析进度 -->
    <el-card v-if="parsing" class="progress-card">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="上传图片" />
        <el-step title="OCR 识别" />
        <el-step title="AI 解析" />
        <el-step title="完成" />
      </el-steps>
    </el-card>

    <!-- OCR 识别结果 -->
    <el-card v-if="ocrText" class="ocr-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>OCR 识别结果</span>
          <el-tag type="info" size="small">{{ ocrText.length }} 字</el-tag>
        </div>
      </template>
      <div class="ocr-text">{{ ocrText }}</div>
    </el-card>

    <!-- 解析结果 -->
    <el-card v-if="parsedResult" class="result-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>解析结果</span>
          <el-tag type="success" size="small">解析成功</el-tag>
        </div>
      </template>

      <!-- 基本信息 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="公司名称">{{ parsedResult.company || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ parsedResult.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ parsedResult.position || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ parsedResult.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ parsedResult.education || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ parsedResult.experience || '-' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ parsedResult.salary || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 技术栈要求 -->
      <div class="section-title">技术栈要求</div>
      <div class="tag-group" v-if="parsedResult.requirements">
        <div class="tag-row">
          <span class="tag-label">必须掌握：</span>
          <el-tag
            v-for="item in parsedResult.requirements.required"
            :key="'req-' + item"
            type="danger"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!parsedResult.requirements.required?.length" class="empty-text">无</span>
        </div>
        <div class="tag-row">
          <span class="tag-label">加分项：</span>
          <el-tag
            v-for="item in parsedResult.requirements.preferred"
            :key="'pref-' + item"
            type="warning"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!parsedResult.requirements.preferred?.length" class="empty-text">无</span>
        </div>
      </div>

      <!-- 软技能要求 -->
      <div class="section-title">软技能要求</div>
      <div class="tag-group" v-if="parsedResult.softSkills">
        <div class="tag-row">
          <span class="tag-label">必须具备：</span>
          <el-tag
            v-for="item in parsedResult.softSkills.required"
            :key="'sreq-' + item"
            type=""
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!parsedResult.softSkills.required?.length" class="empty-text">无</span>
        </div>
        <div class="tag-row">
          <span class="tag-label">加分项：</span>
          <el-tag
            v-for="item in parsedResult.softSkills.preferred"
            :key="'spref-' + item"
            type="info"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!parsedResult.softSkills.preferred?.length" class="empty-text">无</span>
        </div>
      </div>

      <!-- 岗位职责 -->
      <div class="section-title">岗位职责</div>
      <div class="responsibilities-text">{{ parsedResult.responsibilities || '无' }}</div>

      <!-- 操作按钮 -->
      <div class="result-actions">
        <el-button type="primary" @click="$router.push('/jd')">
          <el-icon><List /></el-icon>
          查看 JD 列表
        </el-button>
        <el-button @click="resetParse">
          <el-icon><Refresh /></el-icon>
          继续解析
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, UploadFilled, MagicStick, Document, DataAnalysis, List, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const uploadRef = ref(null)
const selectedFile = ref(null)
const parsing = ref(false)
const activeStep = ref(0)
const ocrText = ref('')
const parsedResult = ref(null)

// 文件选择变化
const handleFileChange = (uploadFile) => {
  selectedFile.value = uploadFile.raw
}

// 文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  ocrText.value = ''
  parsedResult.value = null
}

// 上传前校验
const beforeUpload = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/bmp', 'application/pdf']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('仅支持 JPG / PNG / BMP / PDF 格式的文件')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

// 开始解析
const handleParse = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要上传的 JD 图片')
    return
  }

  parsing.value = true
  activeStep.value = 1
  ocrText.value = ''
  parsedResult.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    activeStep.value = 2

    const res = await request.post('/jd/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000 // OCR + AI 解析可能较慢，设置 2 分钟超时
    })

    activeStep.value = 4

    // 保存结果
    const jdData = res.data
    ocrText.value = jdData.rawText || ''

    // 构建解析结果展示对象
    parsedResult.value = {
      company: jdData.company,
      department: jdData.department,
      position: jdData.position,
      location: jdData.location,
      education: jdData.education,
      experience: jdData.experience,
      salary: jdData.salary,
      requirements: parseJsonField(jdData.requirementsJson),
      softSkills: parseJsonField(jdData.softSkillsJson),
      responsibilities: jdData.responsibilities
    }

    ElMessage.success('JD 解析成功！')
  } catch (e) {
    activeStep.value = 0
    // 错误已在 request 拦截器中处理
  } finally {
    parsing.value = false
  }
}

// 解析 JSON 字段
const parseJsonField = (jsonStr) => {
  if (!jsonStr) return { required: [], preferred: [] }
  try {
    if (typeof jsonStr === 'string') {
      return JSON.parse(jsonStr)
    }
    return jsonStr
  } catch {
    return { required: [], preferred: [] }
  }
}

// 重置，继续解析
const resetParse = () => {
  selectedFile.value = null
  ocrText.value = ''
  parsedResult.value = null
  activeStep.value = 0
  uploadRef.value?.clearFiles()
}
</script>

<style scoped>
.jd-parse-container {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.upload-card {
  margin-bottom: 0;
}

.jd-uploader :deep(.el-upload-dragger) {
  padding: 40px 20px;
}

.upload-actions {
  margin-top: 16px;
  text-align: center;
}

.progress-card {
  margin-bottom: 0;
}

.ocr-card {
  margin-bottom: 0;
}

.ocr-text {
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow-y: auto;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.result-card {
  margin-bottom: 0;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-top: 20px;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.tag-group {
  padding: 0 10px;
}

.tag-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag-label {
  font-size: 13px;
  color: #909399;
  min-width: 80px;
  line-height: 24px;
  flex-shrink: 0;
}

.skill-tag {
  margin: 0;
}

.empty-text {
  font-size: 13px;
  color: #c0c4cc;
  line-height: 24px;
}

.responsibilities-text {
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.result-actions {
  margin-top: 24px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>