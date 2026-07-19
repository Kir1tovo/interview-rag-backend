<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside-container">
      <el-menu :default-active="$route.path" router class="menu-container">
        <div class="logo">AI 面试准备</div>
        <el-menu-item index="/jd">
          <el-icon><Document /></el-icon>
          <span>JD 管理</span>
        </el-menu-item>
        <el-menu-item index="/jd/parse">
          <el-icon><EditPen /></el-icon>
          <span>JD 解析</span>
        </el-menu-item>
        <el-menu-item index="/skill">
          <el-icon><User /></el-icon>
          <span>技能管理</span>
        </el-menu-item>
        <el-menu-item index="/match">
          <el-icon><DataAnalysis /></el-icon>
          <span>匹配分析</span>
        </el-menu-item>
        <el-menu-item index="/plan">
          <el-icon><Reading /></el-icon>
          <span>学习计划</span>
        </el-menu-item>
        <el-sub-menu index="/interview">
          <template #title>
            <el-icon><Search /></el-icon>
            <span>面经知识库</span>
          </template>
          <el-menu-item index="/interview">语义检索</el-menu-item>
          <el-menu-item index="/interview/list">题库列表</el-menu-item>
        </el-sub-menu>
      </el-menu>
      <AgentDialog />
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <span class="username">{{ currentUsername }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, EditPen, User, DataAnalysis, Reading, Search } from '@element-plus/icons-vue'
import AgentDialog from '@/components/AgentDialog.vue'
import request from '@/utils/request'
import { getUsername, clearLoginInfo } from '@/utils/auth'

const router = useRouter()
const currentUsername = getUsername()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return // 取消
  }

  try {
    await request.post('/logout')
  } catch {
    // 即使接口失败也前端退出
  }
  clearLoginInfo()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.logo {
  font-size: 18px;
  font-weight: bold;
  padding: 20px;
  text-align: center;
  color: #409eff;
}
.el-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
}
.aside-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.menu-container {
  flex: 1;
  overflow-y: auto;
}
.el-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  color: #606266;
  font-size: 14px;
}

/* 响应式设计：小屏幕下侧边栏宽度自适应 */
@media screen and (max-width: 768px) {
  .aside-container {
    width: 64px !important;
  }
  .aside-container .el-menu {
    width: 64px;
  }
  .aside-container .el-menu span,
  .aside-container .logo {
    display: none;
  }
  .aside-container .agent-dialog {
    width: 64px;
  }
  .aside-container .agent-toggle span {
    display: none;
  }
}
</style>