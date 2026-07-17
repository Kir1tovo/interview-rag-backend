<template>
  <div class="jd-list-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <h3>JD 列表</h3>
      <el-button type="primary" @click="$router.push('/jd/parse')">
        <el-icon><Plus /></el-icon>
        上传 JD 解析
      </el-button>
    </div>

    <!-- JD 列表表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column label="公司" prop="company" min-width="120">
          <template #default="{ row }">
            {{ row.company || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="岗位" prop="position" min-width="140">
          <template #default="{ row }">
            {{ row.position || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="地点" prop="location" width="100">
          <template #default="{ row }">
            {{ row.location || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="薪资" prop="salary" width="120">
          <template #default="{ row }">
            {{ row.salary || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="学历" prop="education" width="80">
          <template #default="{ row }">
            {{ row.education || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="经验" prop="experience" width="100">
          <template #default="{ row }">
            {{ row.experience || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 格式化时间
const formatTime = (t) => {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/jd/list', {
      params: { page: page.value, size: size.value }
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

// 跳转详情
const goDetail = (id) => {
  router.push(`/jd/detail/${id}`)
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除「${row.company || ''} - ${row.position || ''}」这条 JD 吗？`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    await request.delete(`/jd/${row.id}`)
    ElMessage.success('删除成功')
    // 若本页删完，回到上一页
    if (tableData.value.length === 1 && page.value > 1) {
      page.value--
    }
    fetchList()
  } catch (e) {
    // 错误已在拦截器处理
  }
}

onMounted(fetchList)
</script>

<style scoped>
.jd-list-container {
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

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>