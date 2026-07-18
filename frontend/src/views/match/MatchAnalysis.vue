<template>
  <div class="match-analysis-container" v-loading="loading">
    <!-- 选择 JD -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>选择 JD 进行匹配分析</span>
        </div>
      </template>
      <el-select
        v-model="selectedJdId"
        placeholder="请选择一条 JD"
        style="width: 100%"
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
        style="margin-top: 16px"
        :disabled="!selectedJdId || analyzing"
        @click="handleAnalyze"
      >
        <el-icon><Refresh /></el-icon>
        {{ analyzing ? '分析中...' : '开始匹配分析' }}
      </el-button>
    </el-card>

    <!-- 匹配结果 -->
    <template v-if="matchResult">
      <!-- 匹配度概览 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><PieChart /></el-icon>
            <span>匹配度概览</span>
          </div>
        </template>
        <div class="score-overview">
          <div class="score-item main">
            <div class="score-circle">
              <span class="score-value">{{ matchResult.totalScore }}</span>
              <span class="score-unit">%</span>
            </div>
            <span class="score-label">总体匹配度</span>
          </div>
          <div class="score-item">
            <div class="score-value small">{{ matchResult.techScore }}%</div>
            <span class="score-label">技术栈匹配度</span>
          </div>
          <div class="score-item">
            <div class="score-value small">{{ matchResult.softSkillScore }}%</div>
            <span class="score-label">软技能匹配度</span>
          </div>
        </div>
      </el-card>

      <!-- 技能分类 -->
      <div class="skill-categories">
        <el-card shadow="never" class="category-card">
          <template #header>
            <div class="card-header">
              <el-icon><CircleCheck /></el-icon>
              <span>已掌握</span>
              <span class="count">({{ matchResult.masteredSkills.length }})</span>
            </div>
          </template>
          <div class="tag-group">
            <el-tag
              v-for="skill in matchResult.masteredSkills"
              :key="skill"
              type="success"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <span v-if="!matchResult.masteredSkills.length" class="empty-text">无</span>
          </div>
        </el-card>

        <el-card shadow="never" class="category-card">
          <template #header>
            <div class="card-header">
              <el-icon><Warning /></el-icon>
              <span>需要加强</span>
              <span class="count">({{ matchResult.needImproveSkills.length }})</span>
            </div>
          </template>
          <div class="tag-group">
            <el-tag
              v-for="skill in matchResult.needImproveSkills"
              :key="skill"
              type="warning"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <span v-if="!matchResult.needImproveSkills.length" class="empty-text">无</span>
          </div>
        </el-card>

        <el-card shadow="never" class="category-card">
          <template #header>
            <div class="card-header">
              <el-icon><CircleClose /></el-icon>
              <span>完全不会</span>
              <span class="count">({{ matchResult.notKnownSkills.length }})</span>
            </div>
          </template>
          <div class="tag-group">
            <el-tag
              v-for="skill in matchResult.notKnownSkills"
              :key="skill"
              type="danger"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <span v-if="!matchResult.notKnownSkills.length" class="empty-text">无</span>
          </div>
        </el-card>
      </div>

      <!-- 技术栈匹配详情 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Cpu /></el-icon>
            <span>技术栈匹配详情</span>
          </div>
        </template>
        <el-table :data="matchResult.techMatches" border>
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
              <el-tag :type="row.isRequired ? 'danger' : 'info'" size="small">
                {{ row.isRequired ? '必须' : '加分' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 学习优先级建议 -->
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><List /></el-icon>
            <span>学习优先级建议</span>
          </div>
        </template>
        <el-empty v-if="!matchResult.priorityItems?.length" description="暂无建议" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="item in matchResult.priorityItems"
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
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 分析报告</span>
          </div>
        </template>
        <div class="analysis-report">{{ matchResult.analysisReport }}</div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Setting, Refresh, PieChart, CircleCheck, Warning, CircleClose,
  Cpu, List, ChatDotRound
} from '@element-plus/icons-vue'
import { matchApi, jdApi } from '@/utils/api'

const loading = ref(false)
const analyzing = ref(false)
const jdList = ref([])
const selectedJdId = ref(null)
const matchResult = ref(null)

const getScoreTagType = (score) => {
  if (score === 100) return 'success'
  if (score > 0) return 'warning'
  return 'danger'
}

const getStatusTagType = (status) => {
  if (status === '已掌握') return 'success'
  if (status === '需要加强') return 'warning'
  return 'danger'
}

const fetchJdList = async () => {
  try {
    const res = await jdApi.list({ page: 1, size: 100 })
    jdList.value = res.data.records || []
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const handleAnalyze = async () => {
  analyzing.value = true
  try {
    const res = await matchApi.analyze(selectedJdId.value)
    matchResult.value = res.data
    ElMessage.success('匹配分析完成')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    analyzing.value = false
  }
}

onMounted(fetchJdList)
</script>

<style scoped>
.match-analysis-container {
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

.count {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
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
  gap: 16px;
}

.category-card {
  flex: 1;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin: 0;
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