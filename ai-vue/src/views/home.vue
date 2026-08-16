<template>
    <div class="home-container">
        <div class="content">
            <div class="text">
                <h2 class="title">
                    一次温暖的对话<br/>
                    <span class="highlight-text">化孤独为慰藉</span>
                </h2>
                <p class="description">每个深夜，每个焦虑的时刻，我们都在这里。不必独自承受，让心与心的连接温暖您的每一天</p>
                <div class="hero-actions">
                    <el-button size="large" type="primary" @click="goConsultation">开始倾诉，获得陪伴</el-button>
                    <el-button size="large" class="emotion-btn" @click="goEmotion">记录心情，释放情感</el-button>
                </div>
            </div>
            <div class="robot">
                <el-image style="width: 150px; height: 150px" :src="iconUrl" alt="机器人" class="robot-image" />
            </div>
        </div>
    </div>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const iconUrl = new URL('@/assets/images/robot-fill.png', import.meta.url).href

const requireLogin = (redirect, tip) => {
    if (!localStorage.getItem('token')) {
        ElMessage.warning(tip || '请先登录')
        router.push({ path: '/auth/login', query: { redirect } })
        return false
    }
    return true
}

const goConsultation = () => {
    if (!requireLogin('/consultation', '登录后即可开启AI心理咨询')) return
    router.push('/consultation')
}

const goEmotion = () => {
    if (!requireLogin('/emotion-diary', '登录后即可记录情绪日记')) return
    router.push('/emotion-diary')
}
</script>
<style scoped lang="scss">
.home-container {
    background: linear-gradient(90deg, rgb(74, 156, 140) 0%, rgb(61, 138, 122) 100%) rgba(74, 156, 140, 0.95);
    color: white;
    padding: 5rem 0;
    height: calc(100vh - 285px);
    display: flex;
    align-items: center;
    justify-content: center;
    .content {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 30px;
        .text {
            width: 500px;
            .title {
                font-size: 45px;
                font-weight: bold;
                // line-height: 1.2;
                margin-bottom: 15px;
                .highlight-text {
                    color: #ffd700;
                }
            }
            .hero-actions {
                margin-top: 30px;
                :deep(.emotion-btn) {
                    background-color: transparent;
                    border-color: #fff;
                    color: #fff;
                    &:hover {
                        background-color: rgba(255, 255, 255, 0.1);
                        border-color: #fff;
                        color: #fff;
                    }
                }
            }
        }
        .robot {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 260px;
            height: 260px;
            border-radius: 50%;
            border: 2px solid rgba(255, 255, 255, 0.2);
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1),inset 0 1px 0 rgba(255, 255, 255, 0.3);
        }
    }
}
</style>