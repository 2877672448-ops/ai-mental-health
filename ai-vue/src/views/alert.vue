<template>
  <div class="alert-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">预警总数</div>
            <div class="stat-value">{{ stats.total || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-pending">
          <div class="stat-content">
            <div class="stat-label">未处理</div>
            <div class="stat-value">{{ stats.pending || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-high">
          <div class="stat-content">
            <div class="stat-label">高危预警</div>
            <div class="stat-value">{{ stats.highRisk || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-today">
          <div class="stat-content">
            <div class="stat-label">今日新增</div>
            <div class="stat-value">{{ stats.todayNew || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="filter-area">
          <el-select v-model="filterLevel" placeholder="预警级别" clearable style="width: 140px" @change="loadList">
            <el-option label="低危" :value="1" />
            <el-option label="中危" :value="2" />
            <el-option label="高危" :value="3" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="处理状态" clearable style="width: 140px; margin-left: 10px" @change="loadList">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
            <el-option label="已忽略" :value="2" />
          </el-select>
        </div>
        <div class="action-area">
          <el-date-picker
            v-model="scanDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择扫描日期"
            style="width: 160px; margin-right: 10px"
          />
          <el-button type="primary" :loading="scanning" @click="handleTriggerScan">
            <el-icon><Refresh /></el-icon>
            手动扫描
          </el-button>
          <el-button @click="loadList">
            <el-icon><RefreshRight /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 预警列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="userNickname" label="用户" min-width="120">
          <template #default="{ row }">
            <span>{{ row.userNickname || '用户#' + row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.alertLevel)" effect="dark">
              {{ levelText(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="触发时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">详情</el-button>
            <el-button size="small" type="success" :disabled="row.status !== 0" @click="handleProcess(row, 1)">处理</el-button>
            <el-button size="small" type="warning" :disabled="row.status !== 0" @click="handleProcess(row, 2)">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadList"
        @size-change="loadList"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="预警详情" width="70%" top="5vh">
      <div v-loading="detailLoading" class="detail-content" v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户">{{ detailData.userNickname || '用户#' + detailData.userId }}</el-descriptions-item>
          <el-descriptions-item label="预警级别">
            <el-tag :type="levelTagType(detailData.alertLevel)" effect="dark">
              {{ levelText(detailData.alertLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ formatTime(detailData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人" v-if="detailData.handledBy">
            管理员#{{ detailData.handledBy }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="detailData.handledAt">
            {{ formatTime(detailData.handledAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">触发原因</el-divider>
        <div class="reason-list">
          <el-tag v-for="(reason, idx) in parseJsonArray(detailData.triggerReason)" :key="idx" class="reason-tag">
            {{ reason }}
          </el-tag>
          <span v-if="!parseJsonArray(detailData.triggerReason).length" class="empty-text">无</span>
        </div>

        <el-divider content-position="left">日记内容预览</el-divider>
        <div class="diary-preview">{{ detailData.diaryContentPreview || '无内容' }}</div>

        <el-divider content-position="left">AI 深度分析</el-divider>
        <div class="ai-analysis">{{ detailData.aiAnalysis || 'AI 分析结果为空' }}</div>

        <el-divider content-position="left">推荐科普文章</el-divider>
        <div class="recommend-list">
          <el-tag v-for="(articleId, idx) in parseJsonArray(detailData.recommendedArticles)" :key="idx" type="info" class="reason-tag">
            文章 UUID: {{ articleId }}
          </el-tag>
          <span v-if="!parseJsonArray(detailData.recommendedArticles).length" class="empty-text">无推荐文章</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, RefreshRight } from '@element-plus/icons-vue'
import {
  getAlertPage,
  getAlertDetail,
  handleAlert,
  triggerScan,
  getAlertStats
} from '@/api/alert'

// 状态
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选
const filterLevel = ref(null)
const filterStatus = ref(null)

// 统计
const stats = ref({})

// 手动扫描
const scanDate = ref('')
const scanning = ref(false)

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }
    if (filterLevel.value !== null) params.level = filterLevel.value
    if (filterStatus.value !== null) params.status = filterStatus.value
    const res = await getAlertPage(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载预警列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计
const loadStats = async () => {
  try {
    const res = await getAlertStats()
    stats.value = res.data || {}
  } catch (e) {
    console.warn('加载统计失败', e)
  }
}

// 详情
const handleDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getAlertDetail(row.id)
    detailData.value = res.data
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 处理预警
const handleProcess = async (row, status) => {
  const action = status === 1 ? '处理' : '忽略'
  try {
    await ElMessageBox.confirm(`确认${action}该预警记录？`, '提示', { type: 'warning' })
    await handleAlert(row.id, status)
    ElMessage.success(`${action}成功`)
    loadList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

// 手动触发扫描
const handleTriggerScan = async () => {
  scanning.value = true
  try {
    const params = scanDate.value ? scanDate.value : undefined
    await triggerScan(params)
    ElMessage.success('扫描已触发，请稍后刷新查看结果')
    setTimeout(() => {
      loadList()
      loadStats()
    }, 2000)
  } catch (e) {
    ElMessage.error('扫描触发失败')
  } finally {
    scanning.value = false
  }
}

// 工具方法
const levelText = (lv) => ({ 1: '低危', 2: '中危', 3: '高危' }[lv] || '未知')
const levelTagType = (lv) => ({ 1: 'info', 2: 'warning', 3: 'danger' }[lv] || 'info')
const statusText = (st) => ({ 0: '未处理', 1: '已处理', 2: '已忽略' }[st] || '未知')
const statusTagType = (st) => ({ 0: 'warning', 1: 'success', 2: 'info' }[st] || 'info')

const formatTime = (t) => {
  if (!t) return '-'
  return String(t).replace('T', ' ').substring(0, 19)
}

const parseJsonArray = (str) => {
  if (!str) return []
  try {
    return JSON.parse(str)
  } catch (e) {
    return []
  }
}

onMounted(() => {
  loadList()
  loadStats()
})
</script>

<style lang="scss" scoped>
.alert-page {
  .stat-row {
    margin-bottom: 16px;
  }

  .stat-card {
    .stat-content {
      padding: 8px 0;

      .stat-label {
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #1f2937;
      }
    }

    &.stat-pending .stat-value {
      color: #f59e0b;
    }

    &.stat-high .stat-value {
      color: #ef4444;
    }

    &.stat-today .stat-value {
      color: #3b82f6;
    }
  }

  .toolbar-card {
    margin-bottom: 16px;

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .filter-area {
        display: flex;
        align-items: center;
      }
    }
  }

  .table-card {
    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .detail-content {
    .reason-list, .recommend-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .reason-tag {
        margin: 0;
      }

      .empty-text {
        color: #9ca3af;
        font-size: 14px;
      }
    }

    .diary-preview {
      background: #f9fafb;
      padding: 12px;
      border-radius: 4px;
      line-height: 1.6;
      color: #4b5563;
      white-space: pre-wrap;
    }

    .ai-analysis {
      background: #eff6ff;
      padding: 12px;
      border-radius: 4px;
      line-height: 1.6;
      color: #1e3a8a;
      white-space: pre-wrap;
    }
  }
}
</style>
