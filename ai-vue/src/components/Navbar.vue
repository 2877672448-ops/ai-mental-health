<template>
  <div class="navbar">
    <div class="flex-box">
      <el-button @click="handleCollapse">
        <el-icon><Expand /></el-icon>
      </el-button>
      <p class="page-title">{{ pageTitle }}</p>
    </div>
    <div class="flex-box">
      <el-dropdown trigger="hover" @command="handleCommand">
        <div class="flex-box">
          <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <p class="user-name">admin</p>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { logout } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => {
  return route.meta?.title || ''
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      // 调用后端退出接口（失败也不阻塞本地清理）
      try {
        await logout()
      } catch (e) {
        console.error('退出接口异常:', e)
      }
      // 清理本地登录态
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/auth/login')
    } catch (e) {
      // 用户点了取消，什么都不做
    }
  }
}

const handleCollapse = () => {
  useAdminStore().toggleCollapse()
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid #e5e7eb;

  .flex-box {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }

  .page-title {
    margin-left: 20px;
    font-size: 26px;
    font-weight: bold;
    color: #1f2937;
  }

  .user-name {
    margin: 0 6px;
    font-size: 14px;
    color: #606266;
  }
}
</style>
