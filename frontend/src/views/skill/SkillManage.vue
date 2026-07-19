<template>
  <div class="skill-manage-container">
    <!-- 添加技能弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加技能" width="500px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="技能类型" prop="category">
          <el-radio-group v-model="addForm.category" @change="handleCategoryChange">
            <el-radio value="tech">技术栈</el-radio>
            <el-radio value="soft">软技能</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="技能名称" prop="skillName">
          <el-autocomplete
            v-model="addForm.skillName"
            :fetch-suggestions="fetchSkillSuggestions"
            placeholder="输入技能名称或从下拉选择"
            @select="handleSkillSelect"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-autocomplete>
          <div class="common-skills">
            <span class="label">常用{{ addForm.category === 'soft' ? '软技能' : '技能' }}：</span>
            <el-tag
              v-for="skill in currentCommonSkills.slice(0, 8)"
              :key="skill"
              size="small"
              :type="addForm.category === 'soft' ? 'warning' : 'info'"
              @click="addForm.skillName = skill"
              class="clickable-tag"
            >
              {{ skill }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="掌握程度" prop="level">
          <el-radio-group v-model="addForm.level">
            <el-radio :value="1">了解</el-radio>
            <el-radio :value="2">熟悉</el-radio>
            <el-radio :value="3">精通</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 更新技能弹窗 -->
    <el-dialog v-model="updateDialogVisible" title="修改技能" width="400px">
      <el-form :model="updateForm" label-width="100px">
        <el-form-item label="技能名称">
          <span>{{ updateForm.skillName }}</span>
        </el-form-item>
        <el-form-item label="技能类型">
          <el-radio-group v-model="updateForm.category">
            <el-radio value="tech">技术栈</el-radio>
            <el-radio value="soft">软技能</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="掌握程度">
          <el-radio-group v-model="updateForm.level">
            <el-radio :value="1">了解</el-radio>
            <el-radio :value="2">熟悉</el-radio>
            <el-radio :value="3">精通</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 顶部操作 -->
    <div class="toolbar">
      <h3>技能管理</h3>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加技能
      </el-button>
    </div>

    <!-- 技术栈技能 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>技术栈</span>
          <span class="count">({{ techSkills.length }} 项)</span>
        </div>
      </template>

      <el-empty v-if="!loading && !techSkills.length" description="暂无技术栈技能，请添加" />

      <el-table v-else :data="techSkills" border>
        <el-table-column prop="skillName" label="技能名称" min-width="180" />
        <el-table-column prop="level" label="掌握程度" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="添加时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openUpdateDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 软技能 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>软技能</span>
          <span class="count">({{ softSkills.length }} 项)</span>
        </div>
      </template>

      <el-empty v-if="!loading && !softSkills.length" description="暂无软技能，请添加" />

      <el-table v-else :data="softSkills" border>
        <el-table-column prop="skillName" label="技能名称" min-width="180" />
        <el-table-column prop="level" label="掌握程度" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="添加时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openUpdateDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Monitor, ChatDotRound } from '@element-plus/icons-vue'
import { skillApi } from '@/utils/api'

const loading = ref(false)
const skills = ref([])
const commonTechSkills = ref([])
const commonSoftSkills = ref([])

const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = ref({ skillName: '', level: 2, category: 'tech' })

const updateDialogVisible = ref(false)
const updateForm = ref({ id: null, skillName: '', level: 2, category: 'tech' })

const addRules = {
  skillName: [{ required: true, message: '请输入技能名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择掌握程度', trigger: 'change' }],
  category: [{ required: true, message: '请选择技能类型', trigger: 'change' }]
}

// 按category分类
const techSkills = computed(() => skills.value.filter(s => s.category !== 'soft'))
const softSkills = computed(() => skills.value.filter(s => s.category === 'soft'))

// 当前添加弹窗的常用技能列表
const currentCommonSkills = computed(() => {
  return addForm.value.category === 'soft' ? commonSoftSkills.value : commonTechSkills.value
})

const getLevelText = (level) => {
  const map = { 1: '了解', 2: '熟悉', 3: '精通' }
  return map[level] || '未知'
}

const getLevelTagType = (level) => {
  const map = { 1: 'info', 2: 'warning', 3: 'success' }
  return map[level] || 'info'
}

const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

const fetchSkills = async () => {
  loading.value = true
  try {
    const res = await skillApi.list()
    skills.value = res.data
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const fetchCommonSkills = async () => {
  try {
    const res = await skillApi.common()
    commonTechSkills.value = res.data.allTech || []
    commonSoftSkills.value = res.data.allSoft || []
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const fetchSkillSuggestions = (query, callback) => {
  const pool = addForm.value.category === 'soft' ? commonSoftSkills.value : commonTechSkills.value
  const suggestions = pool
    .filter(skill => skill.toLowerCase().includes(query.toLowerCase()))
    .slice(0, 10)
    .map(skill => ({ value: skill }))
  callback(suggestions)
}

const handleSkillSelect = (item) => {
  addForm.value.skillName = item.value
}

const handleCategoryChange = () => {
  addForm.value.skillName = ''
}

const openAddDialog = () => {
  addForm.value = { skillName: '', level: 2, category: 'tech' }
  addDialogVisible.value = true
}

const handleAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate()

  try {
    await skillApi.add(addForm.value)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    fetchSkills()
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const openUpdateDialog = (row) => {
  updateForm.value = {
    id: row.id,
    skillName: row.skillName,
    level: row.level,
    category: row.category || 'tech'
  }
  updateDialogVisible.value = true
}

const handleUpdate = async () => {
  try {
    await skillApi.update(updateForm.value.id, {
      level: updateForm.value.level,
      category: updateForm.value.category
    })
    ElMessage.success('更新成功')
    updateDialogVisible.value = false
    fetchSkills()
  } catch (e) {
    // 错误已在拦截器处理
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除技能 "${row.skillName}" 吗？`, '删除确认', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
  } catch {
    return
  }

  try {
    await skillApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchSkills()
  } catch (e) {
    // 错误已在拦截器处理
  }
}

onMounted(() => {
  fetchSkills()
  fetchCommonSkills()
})
</script>

<style scoped>
.skill-manage-container {
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

.common-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.common-skills .label {
  font-size: 12px;
  color: #909399;
  line-height: 22px;
}

.clickable-tag {
  cursor: pointer;
}

.clickable-tag:hover {
  opacity: 0.8;
}
</style>