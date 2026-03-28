<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'">
      <div class="logo-box">
        <span v-if="!isCollapse">新闻推荐系统</span>
        <span v-else>News</span>
      </div>
      <!-- 开启 router 模式：index 直接填写路由路径 -->
      <el-menu
        :default-active="$route.path"
        router
        class="el-menu-vertical"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon><template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="/sys">
          <template #title><el-icon><Setting /></el-icon><span>系统信息管理</span></template>
          <!-- 新增部门管理菜单，指定路由路径为 /sys/dept -->
          <el-menu-item index="/sys/dept">部门管理</el-menu-item>
          <el-menu-item index="/sys/staff">员工管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon class="toggle-icon" @click="isCollapse = !isCollapse"><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
          <span class="header-title">控制台</span>
        </div>
        <div class="header-right">
          <el-button link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main>
        <!-- 路由出口：子页面将在这里渲染 -->
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute() // 获取当前路由以设置高亮
const isCollapse = ref(false)

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录?', '提示').then(() => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_role')
    localStorage.removeItem('auth_account')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.el-aside { background-color: #304156; color: #fff; transition: width 0.3s; }
.logo-box { height: 60px; line-height: 60px; text-align: center; color: #fff; background-color: #2b3649; }
.el-menu { border: none; }
.el-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e6e6e6; }
</style>
