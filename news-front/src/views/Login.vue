<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>新闻推荐系统</h2>
      </div>

      <el-tabs v-model="loginForm.role" class="role-tabs" stretch>
        <el-tab-pane label="用户登录" name="user" />
        <el-tab-pane label="管理员登录" name="admin" />
      </el-tabs>

      <el-form :model="loginForm" size="large">
        <el-form-item class="input-item">
          <el-input v-model="loginForm.username" :placeholder="`账号 (${accountConfig.accountExample})`" />
        </el-form-item>

        <el-form-item class="input-item">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">登录</el-button>

        <div class="footer-action">
          <span>还没有账号?</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const loginForm = reactive({ role: 'user', username: '', password: '' })

const roleMap = {
  user: { roleName: '用户', accountExample: 'user' },
  admin: { roleName: '管理员', accountExample: 'admin' }
}

const accountConfig = computed(() => roleMap[loginForm.role])
const roleLabel = computed(() => accountConfig.value.roleName)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.error('请输入账号和密码')
  }

  loading.value = true
  try {
    const res = await axios.post('/api/login', {
      role: loginForm.role,
      account: loginForm.username,
      password: loginForm.password
    })

    if (res.data.code !== 1) {
      return ElMessage.error(res.data.msg || '登录失败')
    }

    const data = res.data.data || {}
    localStorage.setItem('auth_token', data.token || 'mock-token-123')
    localStorage.setItem('auth_role', data.role || loginForm.role)
    localStorage.setItem('auth_account', data.account || loginForm.username)
    localStorage.setItem('auth_user_id', String(data.id || 0))
    localStorage.setItem('auth_name', data.name || loginForm.username)
    localStorage.setItem('auth_avatar', data.avatar || 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U')
    ElMessage.success(`${roleLabel.value}登录成功`)
    const loginRole = data.role || loginForm.role
    router.push(loginRole === 'admin' ? '/home' : '/user/home')
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f3f3f3;
}

.login-card {
  width: 430px;
  border-radius: 10px;
  padding: 18px 18px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.login-header {
  text-align: center;
  margin-bottom: 14px;
}

.login-header h2 {
  margin: 0;
  font-size: 38px;
  font-weight: 700;
  color: #2b2f33;
}

.role-tabs {
  margin-bottom: 18px;
}

.role-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.role-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 0;
}

.role-tabs :deep(.el-tabs__item) {
  color: #666;
  font-size: 15px;
  font-weight: 600;
}

.role-tabs :deep(.el-tabs__item.is-active) {
  color: #22b7ff;
}

.role-tabs :deep(.el-tabs__active-bar) {
  background-color: #22b7ff;
  height: 2px;
}

.input-item {
  margin-bottom: 14px;
}

.input-item :deep(.el-input__wrapper) {
  background: #edf4fc;
  border-radius: 3px;
  box-shadow: 0 0 0 1px #e5edf6 inset;
  height: 42px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 18px;
  font-weight: 500;
  border-radius: 4px;
  margin-top: 14px;
  background: #17a8f7;
  border-color: #17a8f7;
}

.footer-action {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 8px;
  color: #777;
  font-size: 15px;
}
</style>
