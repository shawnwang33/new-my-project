<template>
  <div class="collect-page">
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
          @keyup.enter="fetchCollectList"
          @clear="fetchCollectList"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="top-actions">
        <el-link :underline="false" type="primary">我的收藏</el-link>
        <el-link :underline="false" @click="router.push('/user/history')">历史记录</el-link>
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

    <main class="collect-main">
      <div class="page-title-wrap">
        <h2 class="page-title">我的收藏</h2>
        <el-button
          type="primary"
          :disabled="selectedNewsIds.length === 0"
          @click="handleBatchCancel"
        >
          批量取消收藏
        </el-button>
      </div>

      <div class="card-grid" v-loading="loading">
        <div v-if="collectList.length === 0" class="empty-tip">暂无收藏内容</div>

        <article v-for="item in collectList" :key="item.collectId" class="collect-card" @click="goDetail(item.newsId)">
          <div class="card-check">
            <el-checkbox
              :model-value="selectedNewsIds.includes(item.newsId)"
              @click.stop
              @change="(val) => handleSelectChange(item.newsId, val)"
            />
          </div>
          <img class="cover" :src="item.coverUrl || defaultCover" alt="cover" />

          <div class="card-body">
            <h3 class="title">{{ item.title }}</h3>
            <p class="summary">{{ item.summary || '暂无摘要' }}</p>
            <div class="meta">
              <span>{{ item.categoryName || '新闻' }}</span>
              <span>{{ formatRelativeTime(item.collectTime) }}</span>
            </div>
            <div class="footer">
              <span class="source">{{ item.source || '新闻社' }}</span>
              <el-link
                type="danger"
                :underline="false"
                @click.stop="cancelCollect(item.newsId)"
              >
                取消收藏
              </el-link>
            </div>
          </div>
        </article>
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
const collectList = ref([])
const selectedNewsIds = ref([])
const defaultCover = 'https://dummyimage.com/360x190/f0f2f5/a0a7b4&text=News'

const account = computed(() => localStorage.getItem('auth_account') || '')
const userId = computed(() => Number(localStorage.getItem('auth_user_id') || 0))
const displayName = computed(() => localStorage.getItem('auth_name') || account.value || '用户')
const avatarUrl = computed(() => localStorage.getItem('auth_avatar') || 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U')

const fetchCollectList = async () => {
  if (!userId.value) {
    collectList.value = []
    return
  }
  loading.value = true
  try {
    const res = await axios.get('/api/collect/list', {
      params: {
        userId: userId.value,
        keyword: keyword.value
      }
    })
    if (res.data.code === 1) {
      collectList.value = res.data.data || []
      const validIds = new Set(collectList.value.map(item => item.newsId))
      selectedNewsIds.value = selectedNewsIds.value.filter(id => validIds.has(id))
    } else {
      ElMessage.error(res.data.msg || '收藏列表加载失败')
    }
  } catch (error) {
    ElMessage.error('收藏列表网络异常')
  } finally {
    loading.value = false
  }
}

const handleSelectChange = (newsId, checked) => {
  if (checked) {
    if (!selectedNewsIds.value.includes(newsId)) {
      selectedNewsIds.value.push(newsId)
    }
    return
  }
  selectedNewsIds.value = selectedNewsIds.value.filter(id => id !== newsId)
}

const cancelCollect = async (newsId) => {
  try {
    const res = await axios.post('/api/collect/toggle', {
      userId: userId.value,
      newsId
    })
    if (res.data.code !== 1) {
      return ElMessage.error(res.data.msg || '取消收藏失败')
    }
    selectedNewsIds.value = selectedNewsIds.value.filter(id => id !== newsId)
    ElMessage.success('已取消收藏')
    await fetchCollectList()
  } catch (error) {
    ElMessage.error('取消收藏网络异常')
  }
}

const handleBatchCancel = () => {
  if (selectedNewsIds.value.length === 0) {
    return ElMessage.warning('请先选择要取消收藏的新闻')
  }
  ElMessageBox.confirm(`确定批量取消收藏 ${selectedNewsIds.value.length} 条新闻吗?`, '提示')
    .then(async () => {
      const res = await axios.post('/api/collect/batch-cancel', {
        userId: userId.value,
        newsIds: selectedNewsIds.value
      })
      if (res.data.code !== 1) {
        return ElMessage.error(res.data.msg || '批量取消收藏失败')
      }
      ElMessage.success('批量取消收藏成功')
      selectedNewsIds.value = []
      await fetchCollectList()
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
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} 天前`
  return String(time).replace('T', ' ').slice(0, 16)
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

onMounted(fetchCollectList)
</script>

<style scoped>
.collect-page {
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

.collect-main {
  width: min(1120px, calc(100% - 48px));
  margin: 26px auto;
}

.page-title-wrap {
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  margin: 0;
  font-size: 34px;
  color: #1f2430;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.collect-card {
  position: relative;
  background: #fff;
  border: 1px solid #e7ebf2;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.card-check {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
  width: 22px;
  height: 22px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
}

.collect-card:hover {
  box-shadow: 0 8px 20px rgba(26, 39, 68, 0.08);
}

.cover {
  width: 100%;
  height: 170px;
  object-fit: cover;
  background: #eef1f5;
}

.card-body {
  padding: 12px 12px 10px;
}

.title {
  margin: 0;
  font-size: 22px;
  line-height: 1.45;
  color: #1f2430;
}

.summary {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #6d7684;
  min-height: 42px;
}

.meta {
  display: flex;
  justify-content: space-between;
  color: #9ca4b1;
  font-size: 12px;
}

.footer {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.source {
  color: #9ca4b1;
  font-size: 12px;
}

.empty-tip {
  grid-column: 1 / -1;
  padding: 36px 0;
  text-align: center;
  color: #9ca5b2;
  background: #fff;
  border: 1px dashed #dce2eb;
  border-radius: 8px;
}

@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
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

  .collect-main {
    width: calc(100% - 24px);
    margin: 16px auto;
  }

  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }
}
</style>
