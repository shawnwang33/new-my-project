<template>
  <div class="register-container">
    <el-card class="register-card">
      <div class="register-header">
        <h2>新闻推荐系统</h2>
        <p>用户注册</p>
      </div>

      <el-form ref="formRef" :model="registerForm" :rules="rules" size="large">
        <el-form-item class="input-item" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item class="input-item" prop="account">
          <el-input v-model="registerForm.account" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item class="input-item" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item class="input-item" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" class="submit-btn" @click="handleRegister">注册</el-button>

        <div class="footer-action">
          <span>已有账号?</span>
          <el-link type="primary" :underline="false" @click="router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const formRef = ref(null)

const registerForm = reactive({
  name: '',
  account: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await axios.post('/api/register', {
        role: 'user',
        name: registerForm.name,
        account: registerForm.account,
        password: registerForm.password,
        introduction: null
      })

      if (res.data.code !== 1) {
        return ElMessage.error(res.data.msg || '注册失败')
      }

      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      ElMessage.error('网络请求异常')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f3f3f3;
}

.register-card {
  width: 430px;
  border-radius: 10px;
  padding: 18px 18px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.register-header {
  text-align: center;
  margin-bottom: 18px;
}

.register-header h2 {
  margin: 0;
  font-size: 38px;
  font-weight: 700;
  color: #2b2f33;
}

.register-header p {
  margin: 7px 0 0;
  color: #777;
  font-size: 18px;
  font-weight: 600;
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
