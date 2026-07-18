<template>
  <div class="interview-detail-container" v-loading="loading">
    <!-- 顶部操作 -->
    <div class="toolbar">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <template v-if="detail">
      <!-- 题目详情 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>题目详情</span>
          </div>
        </template>
        <h2 class="question-title">{{ detail.question }}</h2>
        <div class="meta-row">
          <el-tag v-if="detail.category" size="small">{{ detail.category }}</el-tag>
          <el-tag v-if="detail.difficulty" size="small" :type="diffType(detail.difficulty)">{{ diffLabel(detail.difficulty) }}</el-tag>
          <el-tag v-if="detail.company" size="small" type="info">{{ detail.company }}</el-tag>
          <span class="meta-time">{{ formatTime(detail.createdAt) }}</span>
        </div>
      </el-card>

      <!-- 参考答案 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Reading /></el-icon>
            <span>参考答案</span>
          </div>
        </template>
        <div class="text-content" v-if="detail.answer">{{ detail.answer }}</div>
        <el-empty v-else description="暂无参考答案" :image-size="60" />
      </el-card>

      <!-- 解析 -->
      <el-card shadow="never" v-if="detail.analysis">
        <template #header>
          <div class="card-header">
            <el-icon><Memo /></el-icon>
            <span>解析</span>
          </div>
        </template>
        <div class="text-content">{{ detail.analysis }}</div>
      </el-card>

      <!-- 相似题目推荐 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Connection /></el-icon>
            <span>相似题目推荐</span>
          </div>
        </template>
        <div v-if="similarLoading" class="similar-loading">加载中...</div>
        <el-empty v-else-if="!similarList.length" description="暂无相似题目" :image-size="60" />
        <div v-else class="similar-list">
          <el-card v-for="item in similarList" :key="item.id" shadow="hover" class="similar-card" @click="goDetail(item.id)">
            <div class="similar-header">
              <span class="similar-question">{{ item.question }}</span>
              <el-tag v-if="item.similarity != null" size="small" type="success">
                {{ (item.similarity * 100).toFixed(1) }}%
              </el-tag>
            </div>
            <div class="similar-meta">
              <el-tag v-if="item.category" size="small">{{ item.category }}</el-tag>
              <el-tag v-if="item.difficulty" size="small" :type="diffType(item.difficulty)">{{ diffLabel(item.difficulty) }}</el-tag>
              <el-tag v-if="item.company" size="small" type="info">{{ item.company }}</el-tag>
            </div>
          </el-card>
        </div>
      </el-card>
    </template>

    <el-empty v-else-if="!loading" description="题目不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Document, Reading, Memo, Connection } from '@element-plus/icons-vue'
import { interviewApi } from '@/utils/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const similarLoading = ref(false)
const detail = ref(null)
const similarList = ref([])

const diffType = (d) => {
  if (d === 1) return 'success'
  if (d === 2) return 'warning'
  if (d === 3) return 'danger'
  return 'info'
}

const diffLabel = (d) => {
  if (d === 1) return '简单'
  if (d === 2) return '中等'
  if (d === 3) return '困难'
  return '未知'
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

const fetchDetail = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const res = await interviewApi.detail(id)
    detail.value = res.data
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
}

const fetchSimilar = async () => {
  const id = route.params.id
  if (!id) return
  similarLoading.value = true
  try {
    const res = await interviewApi.similar(id, { topK: 5, minSimilarity: 0.5 })
    similarList.value = res.data || []
  } catch (e) {
    similarList.value = []
  } finally {
    similarLoading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/interview/detail/${id}`)
}

// 路由变化时重新加载
watch(() => route.params.id, (newId) => {
  if (newId) {
    detail.value = null
    similarList.value = []
    fetchDetail()
    fetchSimilar()
  }
})

onMounted(() => {
  fetchDetail()
  fetchSimilar()
})
</script>

<style scoped>
.interview-detail-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  gap: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-time {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}

.text-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.similar-loading {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.similar-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.similar-card {
  cursor: pointer;
  transition: border-color 0.2s;
}

.similar-card:hover {
  border-color: #409eff;
}

.similar-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.similar-question {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.similar-meta {
  display: flex;
  gap: 6px;
}
</style>