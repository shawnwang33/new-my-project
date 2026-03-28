<template>
  <div class="user-page">
    <header class="top-bar">
      <div class="brand">
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

    <main class="content">
      <aside class="left-panel panel-card">
        <div class="panel-title">新闻分类</div>
        <ul class="category-list">
          <li
            :class="['category-item', activeCategoryId === null ? 'active' : '']"
            @click="changeCategory(null)"
          >
            全部新闻
          </li>
          <li
            v-for="item in categories"
            :key="item.id"
            :class="['category-item', activeCategoryId === item.id ? 'active' : '']"
            @click="changeCategory(item.id)"
          >
            {{ item.name }}
          </li>
        </ul>
      </aside>

      <section class="center-panel panel-card">
        <div class="panel-title">推荐新闻</div>
        <div v-loading="recommendLoading">
          <div v-if="recommendNews.length === 0" class="empty-tip">暂无相关新闻</div>
          <article v-for="item in recommendNews" :key="item.id" class="news-item" @click="goNewsDetail(item.id)">
            <img class="news-cover" :src="item.coverUrl || defaultCover" alt="news" />
            <div class="news-body">
              <h3 class="news-title">{{ item.title }}</h3>
              <p class="news-summary">{{ item.summary }}</p>
              <div class="news-meta">
                <span>{{ item.categoryName || '-' }}</span>
                <span>{{ formatTime(item.publishTime) }}</span>
              </div>
            </div>
          </article>

          <div class="pager-wrap" v-if="recommendTotal > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="recommendPage"
              :page-size="recommendPageSize"
              :total="recommendTotal"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </section>

      <aside class="right-panel panel-card">
        <div class="panel-title">热门新闻</div>
        <ol class="hot-list" v-loading="hotLoading">
          <li v-for="(item, index) in hotNews" :key="item.id" class="hot-item" @click="goNewsDetail(item.id)">
            <span :class="['rank', index < 3 ? 'top' : '']">{{ index + 1 }}</span>
            <div class="hot-body">
              <p class="hot-title">{{ item.title }}</p>
              <p class="hot-view">{{ item.viewCount || 0 }} 阅读</p>
            </div>
          </li>
        </ol>
      </aside>
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
const account = computed(() => localStorage.getItem('auth_account') || '')
const userId = computed(() => Number(localStorage.getItem('auth_user_id') || 0))
const displayName = computed(() => localStorage.getItem('auth_name') || account.value || '用户')
const avatarUrl = computed(() => localStorage.getItem('auth_avatar') || 'https://dummyimage.com/64x64/e6f4ff/4c8bf5&text=U')

const keyword = ref('')
const activeCategoryId = ref(null)
const categories = ref([])
const recommendNews = ref([])
const hotNews = ref([])
const recommendPage = ref(1)
const recommendPageSize = ref(10)
const recommendTotal = ref(0)

const recommendLoading = ref(false)
const hotLoading = ref(false)

const defaultCover = 'https://dummyimage.com/280x160/f0f2f5/a0a7b4&text=News'

const fetchCategories = async () => {
  try {
    const res = await axios.get('/api/news/categories')
    if (res.data.code === 1) {
      categories.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '分类加载失败')
    }
  } catch (error) {
    ElMessage.error('分类网络异常')
  }
}

const fetchRecommendNews = async () => {
  recommendLoading.value = true
  try {
    const res = await axios.get('/api/news/recommend', {
      params: {
        userId: userId.value || null,
        categoryId: activeCategoryId.value,
        keyword: keyword.value,
        page: recommendPage.value,
        pageSize: recommendPageSize.value
      }
    })
    if (res.data.code === 1) {
      const pageData = res.data.data || {}
      recommendNews.value = pageData.list || []
      recommendTotal.value = pageData.total || 0
    } else {
      ElMessage.error(res.data.msg || '推荐新闻加载失败')
    }
  } catch (error) {
    ElMessage.error('推荐新闻网络异常')
  } finally {
    recommendLoading.value = false
  }
}

const fetchHotNews = async () => {
  hotLoading.value = true
  try {
    const res = await axios.get('/api/news/hot')
    if (res.data.code === 1) {
      hotNews.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '热门新闻加载失败')
    }
  } catch (error) {
    ElMessage.error('热门新闻网络异常')
  } finally {
    hotLoading.value = false
  }
}

const changeCategory = (categoryId) => {
  activeCategoryId.value = categoryId
  recommendPage.value = 1
  fetchRecommendNews()
}

const handleSearch = () => {
  recommendPage.value = 1
  fetchRecommendNews()
}

const handlePageChange = (page) => {
  recommendPage.value = page
  fetchRecommendNews()
}

const goNewsDetail = (id) => {
  router.push(`/user/news/${id}`)
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

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  await fetchCategories()
  await Promise.all([fetchRecommendNews(), fetchHotNews()])
})
</script>

<style scoped>
.user-page {
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

.content {
  display: grid;
  grid-template-columns: 190px minmax(640px, 1fr) 260px;
  gap: 18px;
  padding: 20px 26px;
  align-items: start;
}

.panel-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9edf3;
}

.panel-title {
  padding: 16px 16px 8px;
  font-size: 25px;
  font-weight: 700;
  color: #1f2430;
}

.left-panel {
  padding-bottom: 12px;
  align-self: start;
}

.category-list {
  list-style: none;
  padding: 6px 10px 12px;
  margin: 0;
}

.category-item {
  padding: 10px 12px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  color: #4d5560;
  font-size: 15px;
}

.category-item:hover {
  background: #f3f7ff;
}

.category-item.active {
  background: #eaf4ff;
  color: #1595e8;
  font-weight: 600;
}

.center-panel {
  padding-bottom: 10px;
}

.news-item {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 14px;
  padding: 14px 16px;
  border-top: 1px solid #f0f2f5;
  cursor: pointer;
}

.news-item:hover {
  background: #fafcff;
}

.news-cover {
  width: 180px;
  height: 108px;
  object-fit: cover;
  border-radius: 4px;
  background: #eef1f6;
}

.news-body {
  min-width: 0;
}

.news-title {
  margin: 2px 0 10px;
  font-size: 21px;
  color: #1f2430;
  line-height: 1.4;
}

.news-summary {
  margin: 0 0 12px;
  color: #636c78;
  font-size: 14px;
  line-height: 1.6;
}

.news-meta {
  display: flex;
  gap: 12px;
  color: #98a1ad;
  font-size: 13px;
}

.right-panel {
  padding-bottom: 12px;
  align-self: start;
}

.hot-list {
  margin: 0;
  padding: 8px 12px 12px;
}

.hot-item {
  display: grid;
  grid-template-columns: 24px 1fr;
  gap: 10px;
  margin-bottom: 12px;
  cursor: pointer;
}

.hot-item:hover .hot-title {
  color: #1595e8;
}

.rank {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  background: #e8edf4;
  color: #75839a;
  text-align: center;
  line-height: 22px;
  font-size: 12px;
  font-weight: 600;
}

.rank.top {
  background: #19a8f6;
  color: #fff;
}

.hot-title {
  margin: 0 0 4px;
  color: #2a313d;
  line-height: 1.4;
  font-size: 14px;
}

.hot-view {
  margin: 0;
  color: #9aa3af;
  font-size: 12px;
}

.empty-tip {
  padding: 24px 0 28px;
  text-align: center;
  color: #99a2ad;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px 10px;
  border-top: 1px solid #f0f2f5;
}
</style>
