<template>
  <div class="interview-search-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Search /></el-icon>
          <span>面经检索</span>
        </div>
      </template>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="输入关键词搜索面经题目"
          clearable
          @keyup.enter="handleSearch"
          style="width: 400px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" :loading="searching">
          搜索
        </el-button>
      </div>
      <el-empty v-if="!searching && !results.length" :description="keyword ? '未找到相关面经题目' : '请输入关键词搜索面经题目'" />
      <div v-else-if="results.length" class="results-list">
        <el-card v-for="item in results" :key="item.id" shadow="hover" class="result-card">
          <div class="result-title">{{ item.title || item.question || '面经题目' }}</div>
          <div class="result-meta">
            <el-tag v-if="item.category" size="small">{{ item.category }}</el-tag>
            <el-tag v-if="item.difficulty" size="small" :type="getDiffType(item.difficulty)">{{ item.difficulty }}</el-tag>
            <el-tag v-if="item.company" size="small" type="info">{{ item.company }}</el-tag>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const keyword = ref('')
const searching = ref(false)
const results = ref([])

const getDiffType = (diff) => {
  if (diff === '简单' || diff === 'easy') return 'success'
  if (diff === '中等' || diff === 'medium') return 'warning'
  if (diff === '困难' || diff === 'hard') return 'danger'
  return 'info'
}

const handleSearch = async () => {
  if (!keyword.value.trim()) return
  searching.value = true
  // 面经检索模块尚未完成，暂无实际 API 调用
  results.value = []
  searching.value = false
}

onMounted(() => {
  // 支持从学习计划页面跳转过来时预填关键词
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    handleSearch()
  }
})
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
  margin-bottom: 16px;
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

.result-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.result-meta {
  display: flex;
  gap: 8px;
}
</style>