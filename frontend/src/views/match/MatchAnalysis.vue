<template>
  <div class="match-analysis-container">
    <!-- 顶部操作栏：选择JD + 开始分析 -->
    <el-card shadow="never">
      <div class="toolbar">
        <h3>匹配分析</h3>
        <div class="toolbar-actions">
          <el-select
            v-model="selectedJdId"
            placeholder="选择 JD 进行匹配分析"
            style="width: 300px"
            :disabled="analyzing"
          >
            <el-option
              v-for="jd in jdList"
              :key="jd.id"
              :label="jd.company + ' - ' + jd.position"
              :value="jd.id"
            />
          </el-select>
          <el-button
            type="primary"
            :disabled="!selectedJdId || analyzing"
            :loading="analyzing"
            @click="handleAnalyze"
          >
            <el-icon><Refresh /></el-icon>
            {{ analyzing ? '分析中...' : '开始匹配分析' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 分析历史列表 -->
    <el-card shadow="never" v-loading="historyLoading">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>分析历史记录</span>
          <span class="count">({{ matchList.length }} 条)</span>
        </div>
      </template>

      <el-empty v-if="!historyLoading && !matchList.length" description="暂无匹配分析记录，请先选择 JD 进行分析" />

      <div v-else class="history-list">
        <el-card
          v-for="item in matchList"
          :key="item.id"
          shadow="hover"
          class="history-card"
        >
          <div class="history-card-content">
            <div class="history-info">
              <div class="history-title">
                <el-icon><Document /></el-icon>
                <span>{{ item.jdCompany }} - {{ item.jdPosition }}</span>
                <el-tag :type="getScoreTagType(item.totalScore)" size="small">{{ item.totalScore }}%</el-tag>
              </div>
              <div class="history-meta">
                <span>技术栈 {{ item.techScore }}%</span>
                <span>软技能 {{ item.softSkillScore }}%</span>
                <span>{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>
            <div class="history-actions" @click.stop>
              <el-button size="small" @click="handleView(item)">查看详情</el-button>
              <el-button size="small" type="primary" @click="handleGeneratePlan(item)">生成学习计划</el-button>
              <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 匹配分析详情弹窗 -->
    <el-dialog v-model="detailVisible" title="匹配分析详情" width="900px" destroy-on-close>
      <div v-if="matchDetail" v-loading="detailLoading" class="detail-content">
        <!-- 匹配度概览 -->
        <div class="score-overview">
          <div class="score-item main">
            <div class="score-circle">
              <span class="score-value">{{ matchDetail.totalScore }}</span>
              <span class="score-unit">%</span>
            </div>
            <span class="score-label">总体匹配度</span>
          </div>
          <div class="score-item">
            <div class="score-value small">{{ matchDetail.techScore }}%</div>
            <span class="score-label">技术栈匹配度</span>
          </div>
          <div class="score-item">
            <div class="score-value small">{{ matchDetail.softSkillScore }}%</div>
            <span class="score-label">软技能匹配度</span>
          </div>
        </div>

        <!-- 技能分类 -->
        <div class="skill-categories">
          <div class="category-section">
            <span class="category-label success">已掌握 ({{ matchDetail.masteredSkills?.length || 0 }})</span>
            <div class="tags">
              <el-tag v-for="skill in matchDetail.masteredSkills" :key="skill" type="success" size="small">{{ skill }}</el-tag>
              <span v-if="!matchDetail.masteredSkills?.length" class="empty-text">无</span>
            </div>
          </div>
          <div class="category-section">
            <span class="category-label warning">需要加强 ({{ matchDetail.needImproveSkills?.length || 0 }})</span>
            <div class="tags">
              <el-tag v-for="skill in matchDetail.needImproveSkills" :key="skill" type="warning" size="small">{{ skill }}</el-tag>
              <span v-if="!matchDetail.needImproveSkills?.length" class="empty-text">无</span>
            </div>
          </div>
          <div class="category-section">
            <span class="category-label danger">完全不会 ({{ matchDetail.notKnownSkills?.length || 0 }})</span>
            <div class="tags">
              <el-tag v-for="skill in matchDetail.notKnownSkills" :key="skill" type="danger" size="small">{{ skill }}</el-tag>
              <span v-if="!matchDetail.notKnownSkills?.length" class="empty-text">无</span>
            </div>
          </div>
        </div>

        <!-- 技术栈匹配详情 -->
        <el-card shadow="never" v-if="matchDetail.techMatches?.length">
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>技术栈匹配详情</span>
            </div>
          </template>
          <el-table :data="matchDetail.techMatches" border size="small">
            <el-table-column prop="skillName" label="技能名称" min-width="150" />
            <el-table-column prop="jdLevel" label="JD要求" width="100" />
            <el-table-column prop="userLevel" label="用户掌握" width="100" />
            <el-table-column prop="score" label="匹配得分" width="100">
              <template #default="{ row }">
                <el-tag :type="getScoreTagType(row.score)" size="small">{{ row.score }}分</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="matchStatus" label="匹配状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.matchStatus)" size="small">{{ row.matchStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isRequired" label="是否必须" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isRequired ? 'danger' : 'info'" size="small">{{ row.isRequired ? '必须' : '加分' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 学习优先级建议 -->
        <el-card shadow="never" v-if="matchDetail.priorityItems?.length">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>学习优先级建议</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="item in matchDetail.priorityItems"
              :key="item.skillName"
              :timestamp="'优先级 ' + item.priority"
            >
              <el-card shadow="never" class="timeline-card">
                <div class="priority-item">
                  <span class="skill-name">{{ item.skillName }}</span>
                  <span class="reason">{{ item.reason }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- AI 分析报告 -->
        <el-card shadow="never" v-if="matchDetail.analysisReport">
          <template #header>
            <div class="card-header">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI 分析报告</span>
            </div>
          </template>
          <div class="analysis-report">{{ matchDetail.analysisReport }}</div>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Clock, Document, Cpu, List, ChatDotRound
} from '@element-plus/icons-vue'
import { matchApi, jdApi, learningPlanApi } from '@/utils/api'

const jdList = ref([])
const selectedJdId = ref(null)
const analyzing = ref(false)
const historyLoading = ref(false)
const matchList = ref([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const matchDetail = ref(null)

const getScoreTagType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

const getStatusTagType = (status) => {
  if (status === '已掌握') return 'success'
  if (status === '需要加强') return 'warning'
  return 'danger'
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

const fetchJdList = async () => {
  try {
    const res = await jdApi.list({ page: 1, size: 100 })
    jdList.value = res.data.records || []
  } catch (e) {}
}

const fetchMatchHistory = async () => {
  historyLoading.value = true
  try {
    const res = await matchApi.list()
    matchList.value = res.data || []
  } catch (e) {} finally {
    historyLoading.value = false
  }
}

const handleAnalyze = async () => {
  if (!selectedJdId.value) return
  analyzing.value = true
  try {
    await matchApi.analyze(selectedJdId.value)
    ElMessage.success('匹配分析完成')
    fetchMatchHistory()
  } catch (e) {} finally {
    analyzing.value = false
  }
}

const handleView = async (row) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await matchApi.detail(row.id)
    matchDetail.value = res.data
  } catch (e) {} finally {
    detailLoading.value = false
  }
}

const handleGeneratePlan = async (row) => {
  try {
    await ElMessageBox.confirm(
      '将基于此匹配分析结果生成学习计划，是否继续？',
      '生成学习计划',
      { confirmButtonText: '生成', cancelButtonText: '取消', type: 'info' }
    )
  } catch { return }
  ElMessage.info('学习计划正在后台生成，请稍后在学习计划列表查看')
  learningPlanApi.generate(row.id).catch(() => {})
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条分析记录吗？', '删除确认', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch { return }
  try {
    await matchApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchMatchHistory()
  } catch (e) {}
}

onMounted(() => {
  fetchJdList()
  fetchMatchHistory()
})
</script>

<style scoped>
.match-analysis-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  align-items: center;
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

/* 历史列表卡片 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-card {
  cursor: pointer;
  transition: border-color 0.2s;
}

.history-card:hover {
  border-color: #409eff;
}

.history-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-info {
  flex: 1;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.history-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.history-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 详情弹窗 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.score-overview {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 20px;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-item.main {
  margin-right: auto;
}

.score-circle {
  display: flex;
  align-items: baseline;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: white;
  font-weight: bold;
  margin-bottom: 8px;
}

.score-value {
  font-size: 48px;
}

.score-value.small {
  font-size: 28px;
  color: #606266;
}

.score-unit {
  font-size: 20px;
  margin-left: 4px;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.skill-categories {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.category-label {
  font-size: 14px;
  font-weight: 500;
}

.category-label.success { color: #67c23a; }
.category-label.warning { color: #e6a23c; }
.category-label.danger { color: #f56c6c; }

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.empty-text {
  font-size: 13px;
  color: #c0c4cc;
}

.timeline-card {
  margin-bottom: 0;
}

.priority-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.skill-name {
  font-weight: 600;
  font-size: 14px;
}

.reason {
  font-size: 13px;
  color: #909399;
}

.analysis-report {
  white-space: pre-wrap;
  word-break: break-word;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}
</style>