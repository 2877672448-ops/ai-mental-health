<template>
    <div class="container">
        <div class="title">
            <router-link to="/" class="back-home" style="text-decoration: none; color: inherit; display: inline-flex; align-items: center; gap: 4px;">
                <el-icon><Back /></el-icon>
                <span>返回首页</span>
            </router-link>
            <div class="title-text">
                <h2>登录您的账户</h2>
                <p>请输入您的登录信息</p>
            </div>
        </div>
        <div class="form-container">
            <el-form
                ref="ruleFormRef"
                :model="formData"
                :rules="rules"
                label-position="top"
                @keyup.enter="submitForm(ruleFormRef)"
            >
                <el-form-item label="用户名或邮箱" prop="username">
                    <el-input v-model="formData.username" size="large" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="formData.password" size="large" placeholder="请输入密码" type="password" show-password />
                </el-form-item>
                <el-button
                    class="btn"
                    size="large"
                    type="primary"
                    :loading="submitting"
                    @click="submitForm(ruleFormRef)"
                >登录</el-button>
            </el-form>
            <div class="footer">
                <p>还没有账户？<router-link to="/auth/register">去注册</router-link></p>
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { login } from '@/api/admin'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const ruleFormRef = ref()
const submitting = ref(false)
const router = useRouter()
const route = useRoute()

const formData = reactive({
    username: '',
    password: ''
})
const rules = reactive({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
})

const submitForm = async (formEl) => {
    if (!formEl) return
    try {
        await formEl.validate()
    } catch {
        return
    }
    submitting.value = true
    try {
        const data = await login(formData)
        if (!data || !data.token) {
            ElMessage.error((data && data.msg) || '账号或密码错误')
            return
        }
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
        ElMessage.success('登录成功')
        // 登录成功后按 redirect 优先跳转，其次按用户角色
        const redirect = route.query.redirect
        if (redirect && typeof redirect === 'string') {
            router.replace(redirect)
            return
        }
        if (data.userInfo?.userType === 2) {
            router.replace('/back/dashboard')
        } else {
            router.replace('/')
        }
    } catch (e) {
        const msg = (e && e.response && e.response.data && e.response.data.msg)
            || (e && e.message)
            || '登录失败，请检查账号密码或稍后重试'
        ElMessage.error(msg)
    } finally {
        submitting.value = false
    }
}
</script>
<style scoped lang="scss">
    .container {
        width: 384px;
        .title {
            .back-home {
                margin-bottom: 60px;
            }
            .title-text {
                text-align: center;
                h2 {
                    font-size: 36px;
                    margin-bottom: 10px;
                }
                p {
                    font-size: 18px;
                    color: #6b7280;
                }
            }
        }
        .form-container {
            margin-top: 30px;
            .btn {
                margin-top: 40px;
                width: 100%;
            }
            .footer {
                padding: 30px;
                text-align: center;
            }
        }
    }
</style>