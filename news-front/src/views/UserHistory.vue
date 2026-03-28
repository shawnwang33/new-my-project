<template>
  <div class="history-page">
    <header class="top-bar">
      <div class="brand" @click="router.push('/user/home')">
        <el-icon><Monitor /></el-icon>
        <span>新闻推荐系统</span>
      </div>

      <div class="search-wrap">
        <el-input
          v-model="keyword"
          placeholder="搜索新闻"
          clearable
          @keyup.enter="fetchHistoryList"
          @clear="fetchHistoryList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="top-actions">
        <el-link :underline="false" @click="router.push('/user/collect')">我的收藏</el-link>
        <el-link :underline="false" type="primary">历史记录</el-link>
        <el-dropdown trigger="hover" @command="handleUserCommand">
          <div class="user-menu">
            <img class="avatar" :src="avatarUrl" alt="avatar" />
            <span class="user-name">{{ displayName }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="change-password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="history-main">
      <div class="title-row">
        <h2 class="page-title">阅读历史</h2>
        <div class="title-actions">
          <el-switch v-model="ascOrder" active-text="升序排序" @change="fetchHistoryList" />
          <el-button type="danger" @click="handleClearHistory">清空历史</el-button>
        </div>
      </div>

      <div v-loading="loading">
        <div v-if="historyList.length === 0" class="empty-tip">暂无历史记录</div>

        <div class="timeline" v-else>
          <div class="time-item" v-for="item in historyList" :key="item.recordId" @click="goDetail(item.newsId)">
            <div class="time-dot"></div>
            <div class="time-label">{{ formatRelativeTime(item.browseTime) }}</div>

            <div class="history-card">
              <div class="history-info">
                <h3 class="history-title">{{ item.title }}</h3>
                <p class="history-summary">{{ item.summary || '暂无摘要' }}</p>
                <div class="history-meta">
                  <span>{{ item.source || '新闻社' }}</span>
                  <span>{{ formatRelativeTime(item.publishTime) }}</span>
                </div>
              </div>
              <img class="history-cover" :src="item.coverUrl || defaultCover" alt="cover" />
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Search, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const ascOrder = ref(false)
const historyList = ref([])
const defaultCover = 'https://dummyimage.com/220x120/f0f2f5/a0a7b4&text=News'

const account = computed(() => localStorage.getItem('auth_account') || '')
const userId = computed(() => Number(localStorage.getItem('auth_user_id') || 0))
const displayName = computed(() => localStorage.getItem('auth_name') || account.value || '用户')
const avatarUrl = computed(() => localStorage.getItem('auth_avatar') || 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U')

const fetchHistoryList = async () => {
  if (!userId.value) {
    historyList.value = []
    return
  }
  loading.value = true
  try {
    const res = await axios.get('/api/browse/list', {
      params: {
        userId: userId.value,
        keyword: keyword.value,
        ascOrder: ascOrder.value
      }
    })
    if (res.data.code === 1) {
      historyList.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '历史记录加载失败')
    }
  } catch (error) {
    ElMessage.error('历史记录网络异常')
  } finally {
    loading.value = false
  }
}

const handleClearHistory = () => {
  ElMessageBox.confirm('确定清空全部阅读历史吗?', '提示')
    .then(async () => {
      const res = await axios.post('/api/browse/clear', { userId: userId.value })
      if (res.data.code !== 1) {
        return ElMessage.error(res.data.msg || '清空历史失败')
      }
      ElMessage.success('历史记录已清空')
      await fetchHistoryList()
    })
    .catch(() => {})
}

const goDetail = (newsId) => {
  router.push(`/user/news/${newsId}`)
}

const formatRelativeTime = (time) => {
  if (!time) return '-'
  const date = new Date(String(time).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return String(time).slice(0, 16)
  const diff = Date.now() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  return `${days} 天前`
}

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录?', '提示').then(() => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_role')
    localStorage.removeItem('auth_account')
    localStorage.removeItem('auth_user_id')
    localStorage.removeItem('auth_name')
    localStorage.removeItem('auth_avatar')
    router.push('/login')
  }).catch(() => {})
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    ElMessageBox.alert(
      `用户名：${displayName.value}<br/>账号：${account.value || '-'}`,
      '个人信息',
      { dangerouslyUseHTMLString: true }
    )
    return
  }
  if (command === 'change-password') {
    handleChangePassword()
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}

const handleChangePassword = async () => {
  try {
    const oldRes = await ElMessageBox.prompt('请输入当前密码', '修改密码', {
      inputType: 'password',
      inputPlaceholder: '当前密码',
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码至少6位'
    })
    const newRes = await ElMessageBox.prompt('请输入新密码', '修改密码', {
      inputType: 'password',
      inputPlaceholder: '新密码',
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码至少6位'
    })
    const confirmRes = await ElMessageBox.prompt('请再次输入新密码', '修改密码', {
      inputType: 'password',
      inputPlaceholder: '确认新密码'
    })

    const oldPassword = oldRes.value
    const newPassword = newRes.value
    const confirmPassword = confirmRes.value
    if (newPassword !== confirmPassword) {
      return ElMessage.error('两次输入的新密码不一致')
    }

    const res = await axios.post('/api/user/change-password', {
      userId: Number(localStorage.getItem('auth_user_id') || 0),
      oldPassword,
      newPassword
    })
    if (res.data.code !== 1) {
      return ElMessage.error(res.data.msg || '修改密码失败')
    }

    ElMessage.success('密码修改成功，请重新登录')
    handleLogout()
  } catch (e) {
    if (e === 'cancel' || e === 'close') {
      return
    }
    ElMessage.error('修改密码网络异常')
  }
}

onMounted(fetchHistoryList)
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background: #f2f4f9;
}

.top-bar {
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e8ebf0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  color: #1fa8f9;
  font-weight: 600;
  cursor: pointer;
}

.search-wrap {
  width: 420px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  color: #4b5563;
  font-size: 14px;
}

.arrow {
  color: #8c96a3;
  font-size: 13px;
}

.history-main {
  width: min(980px, calc(100% - 48px));
  margin: 24px auto;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.page-title {
  margin: 0;
  font-size: 34px;
  color: #1f2430;
}

.title-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.timeline {
  position: relative;
  padding-left: 22px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 9px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e2e7ef;
}

.time-item {
  position: relative;
  margin-bottom: 14px;
  cursor: pointer;
}

.time-dot {
  position: absolute;
  left: -18px;
  top: 18px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d4dbe6;
  border: 2px solid #f2f4f9;
}

.time-label {
  margin-bottom: 8px;
  color: #a0a8b5;
  font-size: 12px;
}

.history-card {
  background: #fff;
  border: 1px solid #e8ecf2;
  border-radius: 6px;
  padding: 16px;
  display: grid;
  grid-template-columns: 1fr 160px;
  gap: 12px;
  align-items: center;
}

.history-card:hover {
  box-shadow: 0 8px 20px rgba(26, 39, 68, 0.08);
}

.history-title {
  margin: 0;
  font-size: 20px;
  line-height: 1.5;
  color: #202631;
}

.history-summary {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #687282;
}

.history-meta {
  display: flex;
  gap: 10px;
  color: #9ca4b1;
  font-size: 12px;
}

.history-cover {
  width: 160px;
  height: 90px;
  border-radius: 4px;
  object-fit: cover;
  background: #eef1f5;
}

.empty-tip {
  background: #fff;
  border: 1px dashed #dce2eb;
  border-radius: 8px;
  padding: 36px 0;
  text-align: center;
  color: #9ca5b2;
}

@media (max-width: 900px) {
  .top-bar {
    height: auto;
    gap: 10px;
    padding: 10px 16px;
    flex-wrap: wrap;
  }

  .search-wrap {
    width: 100%;
  }

  .history-main {
    width: calc(100% - 24px);
    margin: 16px auto;
  }

  .title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .history-card {
    grid-template-columns: 1fr;
  }

  .history-cover {
    width: 100%;
    height: 150px;
  }
}
</style>
