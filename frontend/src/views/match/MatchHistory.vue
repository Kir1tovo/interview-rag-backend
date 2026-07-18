<template>
  <div class="match-history-container" v-loading="loading">
    <div class="toolbar">
      <h3>匹配分析历史</h3>
      <el-button @click="$router.push('/match')">
        <el-icon><Plus /></el-icon>
        重新分析
      </el-button>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>分析历史记录</span>
          <span class="count">({{ total }} 条)</span>
        </div>
      </template>

      <el-empty v-if="!loading && !matchList.length" description="暂无匹配分析记录" />

      <el-table v-else :data="matchList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="jdCompany" label="公司" min-width="120" />
        <el-table-column prop="jdPosition" label="职位" min-width="150" />
        <el-table-column prop="totalScore" label="总体匹配度" width="120">
          <template #default="{ row }">
            <div class="score-cell">
              <span class="score-num" :class="getScoreClass(row.totalScore)">{{ row.totalScore }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="techScore" label="技术栈匹配度" width="120">
          <template #default="{ row }">
            <span class="score-num small">{{ row.techScore }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="softSkillScore" label="软技能匹配度" width="120">
          <template #default="{ row }">
            <span class="score-num small">{{ row.softSkillScore }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="分析时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看详情</el-button>
            <el-button size="small" type="primary" @click="handleGeneratePlan(row)">生成学习计划</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 16px; justify-content: center"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchMatchHistory"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="匹配分析详情" width="800px">
      <div v-if="matchDetail" class="detail-content">
        <div class="detail-header">
          <span class="jd-title">{{ matchDetail.jdCompany }} - {{ matchDetail.jdPosition }}</span>
          <span class="detail-score" :class="getScoreClass(matchDetail.totalScore)">
            总体匹配度 {{ matchDetail.totalScore }}%
          </span>
        </div>

        <div class="detail-section">
          <h4>匹配度概览</h4>
          <div class="detail-scores">
            <div class="detail-score-item">
              <span class="label">技术栈匹配度</span>
              <span class="value">{{ matchDetail.techScore }}%</span>
            </div>
            <div class="detail-score-item">
              <span class="label">软技能匹配度</span>
              <span class="value">{{ matchDetail.softSkillScore }}%</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>技能分类</h4>
          <div class="detail-tags">
            <div class="tag-section">
              <span class="tag-label success">已掌握</span>
              <div class="tags">
                <el-tag
                  v-for="skill in matchDetail.masteredSkills"
                  :key="skill"
                  type="success"
                  size="small"
                >{{ skill }}</el-tag>
                <span v-if="!matchDetail.masteredSkills?.length" class="empty-tags">无</span>
              </div>
            </div>
            <div class="tag-section">
              <span class="tag-label warning">需要加强</span>
              <div class="tags">
                <el-tag
                  v-for="skill in matchDetail.needImproveSkills"
                  :key="skill"
                  type="warning"
                  size="small"
                >{{ skill }}</el-tag>
                <span v-if="!matchDetail.needImproveSkills?.length" class="empty-tags">无</span>
              </div>
            </div>
            <div class="tag-section">
              <span class="tag-label danger">完全不会</span>
              <div class="tags">
                <el-tag
                  v-for="skill in matchDetail.notKnownSkills"
                  :key="skill"
                  type="danger"
                  size="small"
                >{{ skill }}</el-tag>
                <span v-if="!matchDetail.notKnownSkills?.length" class="empty-tags">无</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>AI 分析报告</h4>
          <div class="report-content">{{ matchDetail.analysisReport }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock } from '@element-plus/icons-vue'
import { matchApi, learningPlanApi } from '@/utils/api'

const router = useRouter()

const loading = ref(false)
const matchList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const detailDialogVisible = ref(false)
const matchDetail = ref(null)

const getScoreClass = (score) => {
  if (score >= 80) return 'high'
  if (score >= 60) return 'medium'
  return 'low'
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

const fetchMatchHistory = async () => {
  loading.value = true
  try {
    const res = await matchApi.list()
    matchList.value = res.data
    total.value = res.data.length
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const handleView = async (row) => {
  try {
    const res = await matchApi.detail(row.id)
    matchDetail.value = res.data
    detailDialogVisible.value = true
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条分析记录吗？', '删除确认', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }

  try {
    await matchApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchMatchHistory()
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const handleGeneratePlan = async (row) => {
  try {
    await ElMessageBox.confirm(
      '将基于此匹配分析结果生成学习计划，是否继续？',
      '生成学习计划',
      { confirmButtonText: '生成', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  ElMessage.info('学习计划正在后台生成，请稍后在学习计划列表查看')
  learningPlanApi.generate(row.id).catch(() => {})
}

onMounted(fetchMatchHistory)
</script>

<style scoped>
.match-history-container {
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

.score-cell {
  display: flex;
  align-items: center;
}

.score-num {
  font-size: 16px;
  font-weight: bold;
}

.score-num.small {
  font-size: 14px;
}

.score-num.high {
  color: #67c23a;
}

.score-num.medium {
  color: #e6a23c;
}

.score-num.low {
  color: #f56c6c;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.jd-title {
  font-size: 18px;
  font-weight: 600;
}

.detail-score {
  font-size: 18px;
  font-weight: bold;
}

.detail-score.high {
  color: #67c23a;
}

.detail-score.medium {
  color: #e6a23c;
}

.detail-score.low {
  color: #f56c6c;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
}

.detail-scores {
  display: flex;
  gap: 30px;
}

.detail-score-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-score-item .label {
  font-size: 13px;
  color: #909399;
}

.detail-score-item .value {
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

.detail-tags {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tag-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-label {
  font-size: 14px;
  font-weight: 500;
}

.tag-label.success {
  color: #67c23a;
}

.tag-label.warning {
  color: #e6a23c;
}

.tag-label.danger {
  color: #f56c6c;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-tags {
  font-size: 13px;
  color: #c0c4cc;
}

.report-content {
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