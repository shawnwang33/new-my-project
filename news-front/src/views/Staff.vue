<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>员工管理</h2>
      <el-button type="primary">新增员工</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" border stripe>
      <el-table-column prop="id" label="工号" width="120"></el-table-column>
      <el-table-column prop="name" label="姓名" width="120"></el-table-column>
      <el-table-column prop="dept" label="部门"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === '在职' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
const tableData = [
  { id: '1001', name: '张三', dept: '内容审核部', status: '在职' },
  { id: '1002', name: '李四', dept: '算法工程部', status: '在职' },
]
</script>