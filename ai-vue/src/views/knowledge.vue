<template>
    <div v-loading="loading">
        <PageHead title="知识文章">
            <template #buttons>
                <el-button @click="handleEdit({})" type="primary">新增</el-button>
            </template>
        </PageHead>
        <TableSearch :formItem="formItem" @search="handleSearch" />
        <el-table :data="tableData" style="width: 100%;margin-top: 25px">
            <el-table-column width="450" label="文章标题" fixed="left">
                <template #default="scope">
                    <div style="display: flex; align-items: center">
                        <el-icon><Timer /></el-icon>
                        <span>{{ scope.row.title }}</span>
                    </div>
                </template>
            </el-table-column>
             <el-table-column  label="分类" width="200">
                <template #default="scope">
                    <div style="display: flex; align-items: center">
                        <el-icon><Timer /></el-icon>
                        <span>{{ categoryMap[scope.row.categoryId] || scope.row.categoryName || '-' }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="authorName" label="作者" width="150" />
            <el-table-column prop="readCount" label="阅读量" width="150" />
            <el-table-column prop="updatedAt" label="发布时间" width="180" />
            <el-table-column  label="操作" width="240" fixed="right">
                <template #default="scope">
                    <el-button @click="handleEdit(scope.row)" text type="primary">编辑</el-button>
                    <el-button @click="handlePublish(scope.row)" v-if="scope.row.status === 0 || scope.row.status === 2" text type="success">发布</el-button>
                    <el-button @click="handleUnpublish(scope.row)" v-if="scope.row.status === 1" text type="warning">下线</el-button>
                    <el-button @click="handleDelete(scope.row)" text type="danger">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
         style="margin-top: 25px; justify-content: flex-end;"
         background
         :current-page="pagination.currentPage"
         :page-size="pagination.size"
         :page-sizes="[10, 20, 50, 100]"
         layout="total, sizes, prev, pager, next, jumper"
         :total="pagination.total"
         @current-change="handlePageChange"
         @size-change="handleSizeChange"
         />
         <ArticleDialog v-model:modelValue="dialogVisible" :article="currentArticle" :categories="categories" @success="handleSuccess" />
    </div>
</template>
<script setup>
import { onMounted, ref, reactive } from 'vue'
import PageHead from '@/components/PageHead.vue'
import TableSearch from '@/components/TableSearch.vue'
import { categoryTree, articlePage, getArticleDetail, changeArticleStatus, deleteArticle } from '@/api/admin'
import ArticleDialog from '@/components/ArticleDialog.vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const loading = ref(false)

// 保存最近一次的查询条件，供分页复用
const latestSearchParams = ref({})

// 用 reactive 包装整个 formItem，options 的变更才能触发子组件更新
const formItem = reactive([
    { comp: 'input', prop: 'title', label: '文章标题', placeholder: '请输入文章标题' },
    { comp: 'select', prop: 'categoryId', label: '分类', placeholder: '请选择分类', options: [] },
    { comp: 'select', prop: 'status', label: '状态', placeholder: '请选择状态', options: [
       { label: '草稿', value: '0' },
       { label: '已发布', value: '1' },
       { label: '已下线', value: '2' }
    ]}
])

// 分页参数
const pagination = reactive({
    currentPage: 1,
    size: 10,
    total: 0
})

const handleSearch = async (formData) => {
    // TableSearch 会传 formData；分页 change 不传，这时复用上次的条件
    if (formData) {
        latestSearchParams.value = { ...formData }
        // 切条件时，页码重置为 1
        pagination.currentPage = 1
    }
    const params = {
        current: pagination.currentPage,
        size: pagination.size,
        ...latestSearchParams.value
    }
    loading.value = true
    try {
        const { records = [], total = 0 } = (await articlePage(params)) || {}
        tableData.value = records
        pagination.total = total
    } catch (e) {
        console.error('加载文章列表失败:', e)
        ElMessage.error((e?.response?.data?.msg) || '加载文章列表失败')
        tableData.value = []
        pagination.total = 0
    } finally {
        loading.value = false
    }
}

const handlePageChange = (page) => {
    pagination.currentPage = page
    handleSearch()
}

const handleSizeChange = (size) => {
    pagination.size = size
    pagination.currentPage = 1
    handleSearch()
}

// 分类映射
const categoryMap = reactive({})
// 分类列表
const categories = ref([])

// 列表数据
const tableData = ref([])

// 新增和编辑
const dialogVisible = ref(false)
const currentArticle = ref(null)
const handleSuccess = () => {
    dialogVisible.value = false
    handleSearch()
}
const handleEdit = async (row) => {
    if (!row.id) {
        currentArticle.value = null
        dialogVisible.value = true
        return
    }
    try {
        const res = await getArticleDetail(row.id)
        currentArticle.value = res
        dialogVisible.value = true
    } catch (e) {
        ElMessage.error('加载文章详情失败')
    }
}

const handlePublish = (row) => {
    ElMessageBox.confirm(
        `确认发布文章《${row.title}》吗？`,
        '确认',
        {
            confirmButtonText: '确认发布',
            cancelButtonText: '取消',
            type: 'info'
        }
    ).then(() => {
        changeArticleStatus(row.id, { status: 1 })
            .then(() => {
                ElMessage.success('发布成功')
                handleSearch()
            })
            .catch(e => ElMessage.error('发布失败：' + (e?.response?.data?.msg || e.message || '')))
    })
}

const handleUnpublish = (row) => {
    ElMessageBox.confirm(
        `确认下线文章《${row.title}》吗？`,
        '确认',
        {
            confirmButtonText: '确认下线',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        changeArticleStatus(row.id, { status: 2 })
            .then(() => {
                ElMessage.success('下线成功')
                handleSearch()
            })
            .catch(e => ElMessage.error('下线失败：' + (e?.response?.data?.msg || e.message || '')))
    })
}

const handleDelete = (row) => {
    ElMessageBox.confirm(
        `确认删除文章《${row.title}》吗？删除后不可恢复。`,
        '确认',
        {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'danger'
        }
    ).then(() => {
        deleteArticle(row.id)
            .then(() => {
                ElMessage.success('删除成功')
                handleSearch()
            })
            .catch(e => ElMessage.error('删除失败：' + (e?.response?.data?.msg || e.message || '')))
    })
}

onMounted(async () => {
    try {
        const data = (await categoryTree()) || []
        categories.value = data.map(item => {
            categoryMap[item.id] = item.categoryName
            return {
                label: item.categoryName,
                value: item.id
            }
        })
        // formItem 本身是 reactive，直接赋值 options 会触发更新
        formItem[1].options = categories.value
    } catch (e) {
        console.error('加载分类失败:', e)
        ElMessage.warning('分类加载失败，可稍后重试')
    }
    // 获取列表
    handleSearch()
})
</script>
