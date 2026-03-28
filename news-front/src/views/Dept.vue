<template>
  <div>
    <!-- 顶部标题和操作按钮 -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2 style="margin: 0;">部门管理</h2>
      <el-button type="primary" @click="handleAdd">+ 新增部门</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" style="width: 100%" border stripe>
      <!-- 序号列 -->
      <el-table-column type="index" label="序号" width="100" align="center" />
      
      <!-- 部门名称列 -->
      <el-table-column prop="name" label="部门名称" align="center" />
      
      <!-- 最后修改时间列 -->
      <el-table-column prop="updateTime" label="最后修改时间" align="center" />
      
      <!-- 操作列 -->
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">修改</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/修改部门 对话框 (弹窗) -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="30%">
      <el-form ref="formRef" :model="deptForm" :rules="rules" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <!-- 数据库定义为 varchar(10)，所以限制最大长度为 10 -->
          <el-input v-model="deptForm.name" placeholder="请输入部门名称" maxlength="10" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 表格数据相关 ---
const tableData = ref([])
const loading = ref(false)

// --- 弹窗与表单相关 ---
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)

// 绑定的表单数据对象
const deptForm = reactive({
  id: null,
  name: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ]
}

// ================= API 请求逻辑 =================

// 1. 获取部门列表 (GET /depts)
const fetchDeptList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/depts')
    if (response.data.code === 1) {
      tableData.value = response.data.data
    } else {
      ElMessage.error(response.data.msg || '获取部门数据失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

// 2. 点击新增按钮打开弹窗
const handleAdd = () => {
  dialogTitle.value = '新增部门'
  deptForm.id = null // 新增时 ID 为空
  deptForm.name = ''
  dialogVisible.value = true
  
  // 清除上次的表单校验结果
  if (formRef.value) formRef.value.clearValidate()
}

// 3. 点击修改按钮打开弹窗并回显数据 (GET /depts/{id})
const handleEdit = async (row) => {
  dialogTitle.value = '修改部门'
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
  
  try {
    // 调用后端根据 ID 查询单条数据的接口进行回显
    const res = await axios.get(`/api/depts/${row.id}`)
    if (res.data.code === 1) {
      deptForm.id = res.data.data.id
      deptForm.name = res.data.data.name
    } else {
      ElMessage.error(res.data.msg || '获取部门详情失败')
    }
  } catch (error) {
    ElMessage.error('网络请求异常')
  }
}

// 4. 提交表单 (新增 POST /depts 或 修改 PUT /depts)
const submitForm = async () => {
  if (!formRef.value) return
  
  // 触发表单统一校验
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (deptForm.id) {
          // 包含 ID，调用修改接口 (PUT)
          res = await axios.put('/api/depts', deptForm)
        } else {
          // 没有 ID，调用新增接口 (POST)
          res = await axios.post('/api/depts', deptForm)
        }
        
        if (res.data.code === 1) {
          ElMessage.success('操作成功')
          dialogVisible.value = false // 关闭弹窗
          fetchDeptList() // 刷新表格数据
        } else {
          ElMessage.error(res.data.msg || '操作失败')
        }
      } catch (error) {
        ElMessage.error('网络请求异常')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 5. 点击删除按钮 (DELETE /depts?id=xxx)
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除部门【${row.name}】吗？删除后不可恢复！`,
    '删除警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      // 你的后端接收请求参数 public Result delete(Integer id)
      // 在 axios 中使用 params 传递 query 参数 (?id=xxx)
      const res = await axios.delete('/api/depts', { params: { id: row.id } })
      
      if (res.data.code === 1) {
        ElMessage.success('删除成功')
        fetchDeptList() // 刷新表格
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('网络请求异常')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 页面挂载时初始化数据
onMounted(() => {
  fetchDeptList()
})
</script>
