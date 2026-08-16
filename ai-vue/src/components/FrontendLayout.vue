<template>
    <div class="frontend-layout">
        <div class="navbar-container">
            <div class="brand-section">
                <router-link to="/" class="brand-link">
                    <el-image style="width: 50px; height: 50px" :src="iconUrl" alt="品牌logo" class="brand-logo" />
                    <h1 class="brand-name">心理健康AI助手</h1>
                </router-link>
            </div>
            <div class="nav-section">
                <router-link to="/" class="nav-link">首页</router-link>
                <router-link to="/consultation" class="nav-link" v-if="isLoggedIn">AI咨询</router-link>
                <router-link to="/emotion-diary" class="nav-link" v-if="isLoggedIn">情绪日记</router-link>
                <router-link to="/knowledge" class="nav-link">知识库</router-link>
                <el-dropdown v-if="isLoggedIn" trigger="click" @command="handleDropdownCommand">
                    <span class="user-entry nav-link" style="display: inline-flex; align-items: center; gap: 6px; cursor: pointer;">
                        <el-avatar :size="32" :src="userInfo?.avatar">
                            {{ userInfo?.nickname?.charAt?.(0) || userInfo?.username?.charAt?.(0) || 'U' }}
                        </el-avatar>
                        <span class="username">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <template v-else>
                    <router-link to="/auth/login" class="nav-link">登录</router-link>
                    <router-link to="/auth/register" class="nav-link">
                        <el-button type="primary">注册</el-button>
                    </router-link>
                </template>
            </div>
        </div>
        <div class="main-content">
            <router-view></router-view>
        </div>
        <div class="footer-container">
            <div class="footer-bottom">
                <p>&copy; 2026 心理健康AI助手. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>
<script setup>
import { computed, watch } from 'vue'
import { logout } from '@/api/admin'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const iconUrl = new URL('@/assets/images/机器人.png', import.meta.url).href

const isLoggedIn = computed(() => {
    try {
        return !!localStorage.getItem('token')
    } catch {
        return false
    }
})

const userInfo = computed(() => {
    try {
        const raw = localStorage.getItem('userInfo')
        return raw ? JSON.parse(raw) : null
    } catch {
        return null
    }
})

// 用 route.path 作为"触发器"，保证每次路由切换时都重新评估 isLoggedIn/userInfo（computed 本身是惰性的）
// 无需读值，只要建立依赖关系即可
watch(() => route.path, () => {})

const doLogout = async () => {
    try {
        await logout()
    } catch (e) {
        console.error('退出接口异常:', e)
    }
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
}

const handleDropdownCommand = async (cmd) => {
    if (cmd === 'logout') {
        try {
            await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            await doLogout()
            router.push('/auth/login')
        } catch {
            // 用户取消
        }
    } else if (cmd === 'profile') {
        ElMessage.info('个人中心页面待开发')
    }
}
</script>
<style scoped lang="scss">
.frontend-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #fff;

    .navbar-container {
        max-width: 1200px;
        width: 100%;
        height: 100%;
        margin: 0 auto;
        padding: 10px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .brand-section {
            display: flex;
            align-items: center;

            .brand-link {
                display: flex;
                align-items: center;
                text-decoration: none;
                color: inherit;
            }

            .brand-name {
                margin-left: 10px;
                font-size: 24px;
                font-weight: 600;
                color: #333;
            }
        }

        .nav-section {
            display: flex;
            align-items: center;
            gap: 40px;

            .nav-link {
                color: #4b5563 !important;
                font-size: 16px;
                font-weight: 500;
                text-decoration: none;
                display: inline-block;

                &:hover {
                    color: #4A90E2 !important;
                }
            }

            .user-entry {
                .username {
                    font-weight: 500;
                    color: #374151;
                }
            }
        }
    }

    .main-content {
        flex: 1;
        min-width: 0;
    }

    .footer-container {
        background: #1f2937;
        color: white;
        padding: 15px 0;
        margin-top: auto;

        .footer-bottom {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 10px;
            text-align: center;
        }
    }
}
</style>
