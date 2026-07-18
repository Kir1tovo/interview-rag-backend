<template>
  <div class="interview-search-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Search /></el-icon>
          <span>面经检索</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="query"
          placeholder="输入关键词或自然语言问题搜索面经题目"
          clearable
          @keyup.enter="handleSearch"
          style="width: 500px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" :loading="searching">
          语义搜索
        </el-button>
      </div>

      <!-- 搜索参数 -->
      <div class="search-params">
        <span class="param-label">返回数量：</span>
        <el-input-number v-model="topK" :min="1" :max="20" :step="1" size="small" style="width: 100px" />
        <span class="param-label" style="margin-left: 16px">最低相似度：</span>
        <el-slider v-model="minSimilarity" :min="0" :max="1" :step="0.1" :format-tooltip="v => v.toFixed(1)" style="width: 200px; display: inline-block; vertical-align: middle" />
        <span class="param-value">{{ minSimilarity.toFixed(1) }}</span>
      </div>

      <!-- 搜索结果 -->
      <el-empty v-if="!searching && !hasSearched" description="请输入关键词或问题进行语义搜索" />
      <el-empty v-else-if="!searching && hasSearched && !results.length" description="未找到相关面经题目，请尝试调整搜索词或降低相似度阈值" />
      <div v-else-if="results.length" class="results-list">
        <el-card v-for="item in results" :key="item.id" shadow="hover" class="result-card" @click="goDetail(item.id)">
          <div class="result-header">
            <span class="result-question">{{ item.question }}</span>
            <el-tag v-if="item.similarity != null" size="small" type="success" class="similarity-tag">
              相似度 {{ (item.similarity * 100).toFixed(1) }}%
            </el-tag>
          </div>
          <div class="result-answer">{{ truncate(item.answer, 150) }}</div>
          <div class="result-meta">
            <el-tag v-if="item.category" size="small">{{ item.category }}</el-tag>
            <el-tag v-if="item.difficulty" size="small" :type="diffType(item.difficulty)">{{ diffLabel(item.difficulty) }}</el-tag>
            <el-tag v-if="item.company" size="small" type="info">{{ item.company }}</el-tag>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { interviewApi } from '@/utils/api'

const route = useRoute()
const router = useRouter()

const query = ref('')
const topK = ref(10)
const minSimilarity = ref(0.5)
const searching = ref(false)
const hasSearched = ref(false)
const results = ref([])

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

const truncate = (text, len) => {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

const handleSearch = async () => {
  if (!query.value.trim()) return
  searching.value = true
  hasSearched.value = true
  try {
    const res = await interviewApi.search({
      query: query.value.trim(),
      topK: topK.value,
      minSimilarity: minSimilarity.value
    })
    results.value = res.data || []
  } catch (e) {
    results.value = []
  } finally {
    searching.value = false
  }
}

const goDetail = (id) => {
  router.push(`/interview/detail/${id}`)
}

// 支持从学习计划页面跳转过来时预填关键词
if (route.query.keyword) {
  query.value = route.query.keyword
  handleSearch()
}
</script>

<style scoped>
.interview-search-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.search-params {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 16px;
  color: #606266;
  font-size: 13px;
}

.param-label {
  white-space: nowrap;
}

.param-value {
  margin-left: 8px;
  font-weight: 500;
  color: #409eff;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  cursor: pointer;
  transition: border-color 0.2s;
}

.result-card:hover {
  border-color: #409eff;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.result-question {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.similarity-tag {
  margin-left: 8px;
  flex-shrink: 0;
}

.result-answer {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

.result-meta {
  display: flex;
  gap: 8px;
}
</style>