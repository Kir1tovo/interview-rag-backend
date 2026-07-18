<template>
  <div class="learning-plan-container">
    <!-- 顶部操作栏 -->
    <el-card shadow="never">
      <div class="toolbar">
        <h3>学习计划</h3>
        <div class="toolbar-actions">
          <el-select
            v-model="selectedMatchId"
            placeholder="选择匹配分析记录生成计划"
            style="width: 300px"
            :disabled="generating"
          >
            <el-option
              v-for="item in matchHistoryList"
              :key="item.id"
              :label="`${item.jdCompany} - ${item.jdPosition} (${item.totalScore}%)`"
              :value="item.id"
            />
          </el-select>
          <el-button
            type="primary"
            :disabled="!selectedMatchId || generating"
            :loading="generating"
            @click="handleGenerate"
          >
            <el-icon><MagicStick /></el-icon>
            {{ generating ? '生成中...' : '生成学习计划' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 学习计划列表 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon><Reading /></el-icon>
          <span>我的学习计划</span>
          <span class="count">({{ planList.length }} 条)</span>
        </div>
      </template>

      <el-empty v-if="!loading && !planList.length" description="暂无学习计划，请先进行匹配分析" />

      <div v-else class="plan-list">
        <el-card
          v-for="plan in planList"
          :key="plan.id"
          shadow="hover"
          class="plan-card"
          @click="handleViewDetail(plan)"
        >
          <div class="plan-card-content">
            <div class="plan-info">
              <div class="plan-title">
                <el-icon><Document /></el-icon>
                <span>学习计划 #{{ plan.id }}</span>
                <el-tag type="info" size="small">{{ plan.totalEstimatedHours }} 小时</el-tag>
              </div>
              <div class="plan-meta">
                <span>JD ID: {{ plan.jdId }}</span>
                <span>匹配分析 ID: {{ plan.matchId }}</span>
                <span>{{ formatTime(plan.createdAt) }}</span>
              </div>
            </div>
            <div class="plan-actions" @click.stop>
              <el-button size="small" @click="handleViewDetail(plan)">查看详情</el-button>
              <el-button size="small" type="warning" :loading="plan._regenerating" @click="handleRegenerate(plan)">
                重新生成
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(plan)">删除</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 学习计划详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="`学习计划详情`"
      width="900px"
      destroy-on-close
      class="plan-detail-dialog"
    >
      <div v-if="planDetail" v-loading="detailLoading" class="plan-detail">
        <!-- 总览 -->
        <div class="plan-summary">
          <div class="summary-item">
            <span class="summary-label">总预估时长</span>
            <span class="summary-value">{{ planDetail.totalEstimatedHours }} 小时</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">学习阶段</span>
            <span class="summary-value">{{ planDetail.phases?.length || 0 }} 个阶段</span>
          </div>
        </div>

        <!-- AI 学习建议 -->
        <el-card v-if="planDetail.suggestion" shadow="never" class="suggestion-card">
          <template #header>
            <div class="card-header">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI 学习建议</span>
            </div>
          </template>
          <div class="suggestion-text">{{ planDetail.suggestion }}</div>
        </el-card>

        <!-- 阶段时间线 -->
        <el-timeline>
          <el-timeline-item
            v-for="phase in planDetail.phases"
            :key="phase.phaseNumber"
            :timestamp="`阶段 ${phase.phaseNumber}`"
            placement="top"
            :type="getPhaseType(phase.phaseNumber)"
            size="large"
          >
            <el-card shadow="never" class="phase-card">
              <div class="phase-header">
                <h4>{{ phase.phaseName }}</h4>
                <div class="phase-meta">
                  <el-tag type="info" size="small">{{ phase.durationHours }} 小时</el-tag>
                </div>
              </div>

              <div class="phase-goal">
                <strong>学习目标：</strong>{{ phase.goal }}
              </div>

              <!-- 技能标签 -->
              <div class="phase-skills" v-if="phase.skills?.length">
                <span class="section-label">涉及技能：</span>
                <el-tag
                  v-for="skill in phase.skills"
                  :key="skill"
                  size="small"
                  class="skill-tag"
                >
                  {{ skill }}
                </el-tag>
              </div>

              <!-- 学习内容 -->
              <div class="phase-contents" v-if="phase.contents?.length">
                <div class="section-label">学习内容：</div>
                <div
                  v-for="(content, idx) in phase.contents"
                  :key="idx"
                  class="content-item"
                >
                  <div class="content-header">
                    <span class="content-topic">{{ content.topic }}</span>
                    <el-button
                      size="small"
                      type="primary"
                      link
                      @click="searchInterviewQuestions(content.topic)"
                    >
                      <el-icon><Search /></el-icon>
                      搜索面经
                    </el-button>
                  </div>
                  <div class="content-desc">{{ content.description }}</div>
                  <div class="content-resources" v-if="content.resources?.length">
                    <span class="resource-label">推荐资源：</span>
                    <a
                      v-for="(res, rIdx) in content.resources"
                      :key="rIdx"
                      class="resource-link"
                      href="javascript:void(0)"
                      @click="handleResourceClick(res)"
                    >
                      {{ res }}
                    </a>
                  </div>
                </div>
              </div>

              <!-- 里程碑 -->
              <div class="phase-milestone" v-if="phase.milestone">
                <el-icon><Flag /></el-icon>
                <span><strong>里程碑：</strong>{{ phase.milestone }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Reading, MagicStick, Document, ChatDotRound,
  Search, Flag
} from '@element-plus/icons-vue'
import { learningPlanApi, matchApi } from '@/utils/api'

const router = useRouter()
const route = useRoute()

// 列表相关
const loading = ref(false)
const planList = ref([])

// 生成相关
const matchHistoryList = ref([])
const selectedMatchId = ref(null)
const generating = ref(false)

// 详情相关
const detailVisible = ref(false)
const detailLoading = ref(false)
const planDetail = ref(null)

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

const getPhaseType = (num) => {
  if (num === 1) return 'primary'
  if (num === 2) return 'warning'
  if (num === 3) return 'success'
  return 'info'
}

// 获取学习计划列表
const fetchPlanList = async () => {
  loading.value = true
  try {
    const res = await learningPlanApi.list()
    planList.value = (res.data || []).map(p => ({ ...p, _regenerating: false }))
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

// 获取匹配分析历史（用于生成学习计划）
const fetchMatchHistory = async () => {
  try {
    const res = await matchApi.list()
    matchHistoryList.value = res.data || []
  } catch (e) {
    // 错误已在拦截器处理
  }
}

// 生成学习计划
const handleGenerate = async () => {
  if (!selectedMatchId.value) {
    ElMessage.warning('请先选择一条匹配分析记录')
    return
  }

  generating.value = true
  try {
    const res = await learningPlanApi.generate(selectedMatchId.value)
    ElMessage.success('学习计划生成成功！')
    // 直接展示生成的计划
    planDetail.value = res.data
    detailVisible.value = true
    // 刷新列表
    fetchPlanList()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    generating.value = false
  }
}

// 查看详情
const handleViewDetail = async (plan) => {
  detailVisible.value = true
  detailLoading.value = true
  planDetail.value = null

  try {
    const res = await learningPlanApi.detail(plan.id)
    planDetail.value = res.data
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    detailLoading.value = false
  }
}

// 重新生成
const handleRegenerate = async (plan) => {
  try {
    await ElMessageBox.confirm(
      '重新生成将基于原始匹配分析数据创建新的学习计划，是否继续？',
      '重新生成',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  plan._regenerating = true
  try {
    const res = await learningPlanApi.regenerate(plan.id)
    ElMessage.success('学习计划已重新生成')
    planDetail.value = res.data
    detailVisible.value = true
    fetchPlanList()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    plan._regenerating = false
  }
}

// 删除
const handleDelete = async (plan) => {
  try {
    await ElMessageBox.confirm('确定要删除这条学习计划吗？', '删除确认', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }

  try {
    await learningPlanApi.delete(plan.id)
    ElMessage.success('删除成功')
    fetchPlanList()
  } catch (e) {
    // 错误已在拦截器处理
  }
}

// 5.8: 跳转面经检索页面搜索相关题目
const searchInterviewQuestions = (topic) => {
  router.push({ path: '/interview', query: { keyword: topic } })
}

// 资源链接点击（目前面经模块尚未完成，仅提示）
const handleResourceClick = (resource) => {
  // 如果是 URL 格式则跳转，否则提示
  if (/^https?:\/\//.test(resource)) {
    window.open(resource, '_blank')
  } else {
    // 将资源名称作为关键词搜索面经
    searchInterviewQuestions(resource)
  }
}

onMounted(() => {
  fetchPlanList()
  fetchMatchHistory()

  // 支持从匹配历史页面跳转过来时自动打开详情
  if (route.query.planId) {
    const planId = Number(route.query.planId)
    if (planId) {
      handleViewDetail({ id: planId })
    }
  }
})
</script>

<style scoped>
.learning-plan-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar h3 {
  margin: 0;
  font-size: 18px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.count {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
}

/* 计划列表 */
.plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-card {
  cursor: pointer;
  transition: border-color 0.2s;
}

.plan-card:hover {
  border-color: #409eff;
}

.plan-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}

.plan-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.plan-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 详情 */
.plan-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.plan-summary {
  display: flex;
  gap: 40px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-label {
  font-size: 13px;
  color: #909399;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.suggestion-card {
  margin-bottom: 0;
}

.suggestion-text {
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.phase-card {
  margin-bottom: 0;
}

.phase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.phase-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.phase-meta {
  display: flex;
  gap: 8px;
}

.phase-goal {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.5;
}

.section-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
  margin-bottom: 8px;
  display: block;
}

.phase-skills {
  margin-bottom: 12px;
}

.phase-skills .section-label {
  display: inline;
  margin-right: 8px;
}

.phase-skills .skill-tag {
  margin: 2px 4px 2px 0;
}

.phase-contents {
  margin-bottom: 12px;
}

.content-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.content-item:last-child {
  margin-bottom: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.content-topic {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.content-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 6px;
}

.content-resources {
  font-size: 13px;
  color: #909399;
}

.resource-label {
  margin-right: 4px;
}

.resource-link {
  color: #409eff;
  text-decoration: none;
  margin-right: 12px;
  cursor: pointer;
}

.resource-link:hover {
  text-decoration: underline;
}

.phase-milestone {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-radius: 4px;
  font-size: 13px;
  color: #409eff;
}

.phase-milestone .el-icon {
  color: #e6a23c;
}
</style>