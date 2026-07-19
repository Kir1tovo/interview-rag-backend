<template>
  <div class="interview-list-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <h3>面经题库</h3>
      <el-button type="primary" @click="showImportDialog = true">
        <el-icon><Upload /></el-icon>
        导入面经
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never">
      <div class="filter-bar">
        <el-select v-model="filters.category" placeholder="技术分类" clearable style="width: 150px" @change="fetchList">
          <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filters.difficulty" placeholder="难度" clearable style="width: 120px" @change="fetchList">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
        <el-select v-model="filters.company" placeholder="来源公司" clearable style="width: 150px" @change="fetchList">
          <el-option v-for="c in companyOptions" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <!-- 题目列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column label="题目" prop="question" min-width="250">
          <template #default="{ row }">
            <el-link type="primary" @click="goDetail(row.id)">{{ row.question }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="技术分类" prop="category" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.category" size="small">{{ row.category }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="难度" prop="difficulty" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.difficulty" size="small" :type="diffType(row.difficulty)">{{ diffLabel(row.difficulty) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="来源公司" prop="company" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.company" size="small" type="info">{{ row.company }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 导入对话框 -->
    <el-dialog v-model="showImportDialog" title="导入面经" width="500px" :close-on-click-modal="false">
      <el-upload
        ref="uploadRef"
        drag
        accept=".md"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="() => ElMessage.warning('只能上传一个文件')"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽或点击上传 <em>.md</em> 文件</div>
        <template #tip>
          <div class="el-upload__tip">文件名格式：公司名.md（如"京东.md"），内容以 ## 二级标题拆分题目</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload, View, UploadFilled } from '@element-plus/icons-vue'
import { interviewApi } from '@/utils/api'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const filters = ref({
  category: '',
  difficulty: null,
  company: ''
})

const categoryOptions = ref([
  'Java基础', 'JVM', '集合框架', '并发编程', 'IO', '网络',
  'Spring', 'Spring Boot', 'Spring Cloud', 'MyBatis',
  'Redis', 'MySQL', 'MongoDB', 'Elasticsearch',
  '分布式', '微服务', '消息队列', '分布式锁',
  '设计模式', '算法', '数据结构',
  'Docker', 'K8s', 'CI/CD',
  '前端', 'Linux', '操作系统', '其他'
])

const companyOptions = ref(['京东', '搜狐', '腾讯', '网易', '美团', '百度', '华为', '拼多多'])

const showImportDialog = ref(false)
const importing = ref(false)
const uploadFile = ref(null)

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
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (filters.value.category) params.category = filters.value.category
    if (filters.value.difficulty) params.difficulty = filters.value.difficulty
    if (filters.value.company) params.company = filters.value.company

    const res = await interviewApi.list(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = { category: '', difficulty: null, company: '' }
  page.value = 1
  fetchList()
}

const goDetail = (id) => {
  router.push(`/interview/detail/${id}`)
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const handleImport = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    const res = await interviewApi.importFile(formData)
    ElMessage.success(`导入成功，共导入 ${res.data?.length || 0} 条题目`)
    showImportDialog.value = false
    uploadFile.value = null
    fetchList()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    importing.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.interview-list-container {
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

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>