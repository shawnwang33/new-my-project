<template>
  <div class="detail-page">
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
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="top-actions">
        <el-link :underline="false" @click="router.push('/user/collect')">我的收藏</el-link>
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

    <main class="detail-main">
      <el-card class="detail-card" v-loading="loading">
        <template v-if="detail">
          <h1 class="detail-title">{{ detail.title }}</h1>
          <img class="detail-cover" :src="detail.coverUrl || defaultCover" alt="cover" />

          <div class="detail-meta">
            <span>{{ detail.source || '新闻社' }}</span>
            <span>{{ formatTime(detail.publishTime) }}</span>
            <span>{{ detail.categoryName || '新闻' }}</span>
          </div>

          <blockquote class="detail-summary">{{ detail.summary }}</blockquote>

          <div class="detail-content">
            <p v-for="(line, index) in contentParagraphs" :key="index">{{ line }}</p>
          </div>
          <div class="action-row">
            <el-button
              :type="collectStatus.collected ? 'primary' : 'default'"
              @click="handleToggleCollect"
            >
              {{ collectStatus.collected ? '已收藏' : '收藏' }} ({{ collectStatus.collectCount }})
            </el-button>
            <div class="action-stat">阅读 {{ detail.viewCount || 0 }}</div>
            <div class="action-stat">评论 {{ commentCount }}</div>
          </div>

          <div class="comment-title">全部评论 ({{ commentCount }})</div>

          <div class="comment-editor">
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="3"
              maxlength="300"
              show-word-limit
              placeholder="写下你的评论..."
            />
            <div class="comment-submit">
              <el-button type="primary" :loading="submitLoading" @click="handleSubmitComment">发布评论</el-button>
            </div>
          </div>

          <div class="comment-list">
            <div v-if="commentList.length === 0" class="comment-empty">暂无评论，欢迎抢沙发</div>
            <div v-for="item in commentList" :key="item.id" class="comment-item">
              <div class="comment-head">
                <span class="comment-user">{{ item.userAccount || ('用户' + (item.userId || '')) }}</span>
                <span class="comment-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <p class="comment-content">{{ item.content }}</p>
            </div>
          </div>
        </template>
      </el-card>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Search, ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const loading = ref(false)
const submitLoading = ref(false)
const detail = ref(null)
const commentList = ref([])
const commentCount = ref(0)
const commentForm = ref({ content: '' })
const collectStatus = ref({ collected: false, collectCount: 0 })
const defaultCover = 'https://dummyimage.com/720x420/f0f2f5/a0a7b4&text=News'
const account = computed(() => localStorage.getItem('auth_account') || '')
const userId = computed(() => Number(localStorage.getItem('auth_user_id') || 0))
const displayName = computed(() => localStorage.getItem('auth_name') || account.value || '用户')
const avatarUrl = computed(() => localStorage.getItem('auth_avatar') || 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U')

const contentParagraphs = computed(() => {
  const content = detail.value?.content || ''
  if (!content) return []
  return content.split('\n').map(item => item.trim()).filter(Boolean)
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/news/detail/${route.params.id}`, {
      params: { userId: userId.value || 0 }
    })
    if (res.data.code === 1) {
      detail.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '加载详情失败')
    }
  } catch (error) {
    ElMessage.error('详情网络异常')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await axios.get(`/api/comments/news/${route.params.id}`)
    if (res.data.code === 1) {
      const data = res.data.data || {}
      commentList.value = data.list || []
      commentCount.value = data.count || 0
    } else {
      ElMessage.error(res.data.msg || '评论加载失败')
    }
  } catch (error) {
    ElMessage.error('评论网络异常')
  }
}

const handleSubmitComment = async () => {
  if (!commentForm.value.content || !commentForm.value.content.trim()) {
    return ElMessage.warning('请输入评论内容')
  }

  submitLoading.value = true
  try {
    const res = await axios.post('/api/comments', {
      newsId: Number(route.params.id),
      userAccount: account.value || 'user',
      content: commentForm.value.content
    })
    if (res.data.code !== 1) {
      return ElMessage.error(res.data.msg || '评论发布失败')
    }

    ElMessage.success('评论发布成功')
    commentForm.value.content = ''
    await fetchComments()
  } catch (error) {
    ElMessage.error('评论发布网络异常')
  } finally {
    submitLoading.value = false
  }
}

const fetchCollectStatus = async () => {
  try {
    const res = await axios.get('/api/collect/status', {
      params: {
        userId: userId.value,
        newsId: Number(route.params.id)
      }
    })
    if (res.data.code === 1) {
      collectStatus.value = res.data.data || { collected: false, collectCount: 0 }
    } else {
      ElMessage.error(res.data.msg || '收藏状态加载失败')
    }
  } catch (error) {
    ElMessage.error('收藏状态网络异常')
  }
}

const handleToggleCollect = async () => {
  if (!userId.value) {
    return ElMessage.warning('请先登录后再收藏')
  }
  try {
    const res = await axios.post('/api/collect/toggle', {
      userId: userId.value,
      newsId: Number(route.params.id)
    })
    if (res.data.code === 1) {
      collectStatus.value = res.data.data || collectStatus.value
      ElMessage.success(collectStatus.value.collected ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(res.data.msg || '收藏操作失败')
    }
  } catch (error) {
    ElMessage.error('收藏操作网络异常')
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 16)
}

const handleSearch = () => {
  router.push({
    path: '/user/home',
    query: keyword.value ? { keyword: keyword.value } : {}
  })
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

onMounted(() => {
  fetchDetail()
  fetchComments()
  fetchCollectStatus()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f3f5f9;
}

.top-bar {
  height: 64px;
  padding: 0 26px;
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
  width: 450px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #6f7782;
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

.detail-main {
  padding: 24px;
  display: flex;
  justify-content: center;
}

.detail-card {
  width: 980px;
  border-radius: 8px;
}

.detail-title {
  margin: 4px 0 18px;
  font-size: 36px;
  color: #1f2430;
}

.detail-cover {
  width: 100%;
  max-height: 440px;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 14px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #8f98a5;
  font-size: 13px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.detail-summary {
  margin: 0 0 18px;
  padding: 12px 14px;
  background: #f7f9fc;
  border-left: 4px solid #2da2ef;
  color: #4d5663;
}

.detail-content {
  color: #303742;
  line-height: 1.9;
  font-size: 16px;
}

.detail-content p {
  margin: 0 0 14px;
}

.action-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
}

.action-stat {
  height: 32px;
  width: 120px;
  padding: 0;
  border: 1px solid #e2e7ef;
  border-radius: 0;
  background: #f8fafc;
  color: #6b7481;
  font-size: 13px;
  line-height: 32px;
  text-align: center;
  margin-left: -1px;
}

.action-row :deep(.el-button) {
  width: 120px;
  padding: 0;
  border-radius: 0;
}

.comment-title {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid #edf0f5;
  font-size: 30px;
  font-weight: 600;
}

.comment-editor {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fafbfd;
}

.comment-submit {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  margin-top: 14px;
}

.comment-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 600;
  color: #2d3542;
}

.comment-time {
  font-size: 12px;
  color: #9aa3af;
}

.comment-content {
  margin: 0;
  color: #404855;
  line-height: 1.7;
}

.comment-empty {
  padding: 18px 0;
  color: #97a0ac;
  text-align: center;
}
</style>
