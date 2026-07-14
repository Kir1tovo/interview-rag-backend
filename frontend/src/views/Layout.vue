<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu :default-active="$route.path" router>
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
        <el-menu-item index="/interview">
          <el-icon><Search /></el-icon>
          <span>面经检索</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
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
import { Document, EditPen, User, DataAnalysis, Reading, Search } from '@element-plus/icons-vue'

const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('token')
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
.el-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
}
</style>