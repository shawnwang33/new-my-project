<template>
  <div>
    <h2 class="welcome-text">系统概览</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>今日点击量</template>
          <div style="font-size: 24px; font-weight: bold;">128,846</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>活跃用户</template>
          <div style="font-size: 24px; font-weight: bold; color: #67C23A;">+532</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>算法命中率</template>
          <div style="font-size: 24px; font-weight: bold; color: #409EFF;">8.5%</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.welcome-text { margin-bottom: 20px; color: #303133; }
</style>
