<template>
  <div class="jd-detail-container" v-loading="loading">
    <!-- 顶部操作 -->
    <div class="toolbar">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <el-button type="danger" plain @click="handleDelete">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>

    <template v-if="jd">
      <!-- 基本信息 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>基本信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="公司名称">{{ jd.company || '-' }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ jd.department || '-' }}</el-descriptions-item>
          <el-descriptions-item label="岗位名称">{{ jd.position || '-' }}</el-descriptions-item>
          <el-descriptions-item label="工作地点">{{ jd.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学历要求">{{ jd.education || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经验要求">{{ jd.experience || '-' }}</el-descriptions-item>
          <el-descriptions-item label="薪资范围">{{ jd.salary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(jd.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 技术栈要求 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Cpu /></el-icon>
            <span>技术栈要求</span>
          </div>
        </template>
        <div class="tag-row">
          <span class="tag-label">必须掌握：</span>
          <el-tag
            v-for="item in requirements.required"
            :key="'req-' + item"
            type="danger"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!requirements.required.length" class="empty-text">无</span>
        </div>
        <div class="tag-row">
          <span class="tag-label">加分项：</span>
          <el-tag
            v-for="item in requirements.preferred"
            :key="'pref-' + item"
            type="warning"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!requirements.preferred.length" class="empty-text">无</span>
        </div>
      </el-card>

      <!-- 软技能要求 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><UserFilled /></el-icon>
            <span>软技能要求</span>
          </div>
        </template>
        <div class="tag-row">
          <span class="tag-label">必须具备：</span>
          <el-tag
            v-for="item in softSkills.required"
            :key="'sreq-' + item"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!softSkills.required.length" class="empty-text">无</span>
        </div>
        <div class="tag-row">
          <span class="tag-label">加分项：</span>
          <el-tag
            v-for="item in softSkills.preferred"
            :key="'spref-' + item"
            type="info"
            class="skill-tag"
          >
            {{ item }}
          </el-tag>
          <span v-if="!softSkills.preferred.length" class="empty-text">无</span>
        </div>
      </el-card>

      <!-- 岗位职责 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>岗位职责</span>
          </div>
        </template>
        <div class="text-content">{{ jd.responsibilities || '无' }}</div>
      </el-card>

      <!-- OCR 原文 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><DocumentCopy /></el-icon>
            <span>OCR 识别原文</span>
          </div>
        </template>
        <div class="text-content raw-text">{{ jd.rawText || '无' }}</div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Delete, Document, Cpu, UserFilled, List, DocumentCopy
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const jd = ref(null)

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

const requirements = computed(() => parseJsonField(jd.value?.requirementsJson))
const softSkills = computed(() => parseJsonField(jd.value?.softSkillsJson))

// 格式化时间
const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/jd/${route.params.id}`)
    jd.value = res.data
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

// 删除
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条 JD 记录吗？', '删除确认', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }

  try {
    await request.delete(`/jd/${route.params.id}`)
    ElMessage.success('删除成功')
    router.push('/jd')
  } catch (e) {
    // 错误已在拦截器处理
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.jd-detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.tag-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag-row:last-child {
  margin-bottom: 0;
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

.text-content {
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.raw-text {
  max-height: 400px;
  overflow-y: auto;
}
</style>