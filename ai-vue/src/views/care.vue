<template>
  <div class="care-page">
    <!-- 顶部关怀横幅 -->
    <div class="hero-section">
      <div class="hero-content">
        <el-image :src="heroIcon" class="hero-icon" fit="contain" />
        <div class="hero-text">
          <h1>心灵关怀</h1>
          <p>根据你最近的情绪记录，为你挑选了这些温柔的内容</p>
        </div>
      </div>
    </div>

    <!-- 推荐文章列表 -->
    <div class="content-wrapper">
      <div v-loading="loading" class="article-area">
        <template v-if="articleList.length">
          <div class="section-title">
            <el-icon><Sunrise /></el-icon>
            <span>为你推荐</span>
          </div>
          <div class="article-grid">
            <div
              v-for="item in articleList"
              :key="item.id"
              class="article-card"
              @click="goToArticle(item.id)"
            >
              <div class="cover-wrapper">
                <el-image :src="getCover(item.cover_image)" class="cover" fit="cover">
                  <template #error>
                    <div class="cover-fallback">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <el-tag class="category-tag" type="warning" effect="plain" size="small">
                  {{ item.category_name || '心理科普' }}
                </el-tag>
              </div>
              <div class="card-body">
                <h3 class="card-title">{{ item.title }}</h3>
                <p class="card-summary">{{ item.summary || '点击查看详情' }}</p>
                <div class="card-meta">
                  <span class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ item.author_name || '匿名作者' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ item.read_count || 0 }} 阅读
                  </span>
                  <span class="meta-item" v-if="item.published_at">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(item.published_at) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="empty-state">
          <el-image :src="emptyIcon" class="empty-icon" fit="contain" />
          <h3>暂无推荐内容</h3>
          <p>继续保持记录心情的习惯，我们会在这里为你准备温暖的内容。</p>
          <el-button type="primary" round @click="goToDiary">去记录今天的心情</el-button>
        </div>
      </div>

      <!-- 右侧关怀侧栏 -->
      <aside class="care-aside">
        <div class="aside-card warm-card">
          <div class="aside-icon">
            <el-icon><Sunny /></el-icon>
          </div>
          <h4>给自己一点时间</h4>
          <p>情绪没有好坏之分，每一次记录都是对自己的温柔关照。</p>
        </div>

        <div class="aside-card hotline-card">
          <h4>需要更多支持？</h4>
          <p class="hotline-text">如果你正经历难以承受的情绪，请记得寻求专业帮助：</p>
          <ul class="hotline-list">
            <li>
              <span class="hotline-name">全国心理援助热线</span>
              <span class="hotline-num">400-161-9995</span>
            </li>
            <li>
              <span class="hotline-name">北京心理危机研究与干预中心</span>
              <span class="hotline-num">010-82951332</span>
            </li>
            <li>
              <span class="hotline-name">生命热线</span>
              <span class="hotline-num">400-821-1215</span>
            </li>
          </ul>
          <p class="hotline-tip">你不必独自承受，向他人倾诉是勇敢的表现。</p>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Sunrise, Sunny, View, User, Clock, Picture } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getMyRecommendedArticles } from '@/api/alert'

const router = useRouter()

// 默认占位图（SVG data URI，无外部依赖）
const heroIcon = new URL('@/assets/images/机器人.png', import.meta.url).href
const emptyIcon = 'data:image/svg+xml;utf8,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160">' +
  '<circle cx="80" cy="80" r="72" fill="#fef3c7"/>' +
  '<path d="M80 40a32 32 0 0 0-32 32c0 24 32 48 32 48s32-24 32-48a32 32 0 0 0-32-32z" fill="#fbbf24"/>' +
  '<circle cx="68" cy="70" r="4" fill="#fff"/><circle cx="92" cy="70" r="4" fill="#fff"/>' +
  '</svg>'
)

const loading = ref(false)
const articleList = ref([])

// 加载推荐文章
const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getMyRecommendedArticles()
    articleList.value = Array.isArray(res) ? res : []
  } catch (e) {
    // 401 等已被全局拦截器处理；此处仅兜底
    articleList.value = []
  } finally {
    loading.value = false
  }
}

// 封面图处理：完整 URL 直接用，相对路径拼前缀
const getCover = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://159.75.169.224:1235' + url
}

const formatDate = (t) => {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 10)
}

const goToArticle = (id) => {
  router.push(`/knowledge/article/${id}`)
}

const goToDiary = () => {
  router.push('/emotion-diary')
}

onMounted(() => {
  loadArticles()
})
</script>

<style lang="scss" scoped>
.care-page {
  background: linear-gradient(180deg, #fdfbf7 0%, #faf6f0 100%);
  min-height: calc(100vh - 100px);

  // 顶部横幅
  .hero-section {
    background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 50%, #fb923c 100%);
    padding: 48px 20px;

    .hero-content {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      gap: 20px;

      .hero-icon {
        width: 64px;
        height: 64px;
        flex-shrink: 0;
      }

      .hero-text {
        h1 {
          font-size: 30px;
          font-weight: 700;
          color: #fff;
          margin: 0 0 6px 0;
          letter-spacing: 1px;
        }

        p {
          font-size: 15px;
          color: rgba(255, 255, 255, 0.92);
          margin: 0;
        }
      }
    }
  }

  // 内容区
  .content-wrapper {
    max-width: 1200px;
    margin: 0 auto;
    padding: 28px 20px 40px;
    display: flex;
    gap: 24px;
    align-items: flex-start;

    .article-area {
      flex: 1;
      min-width: 0;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;
        color: #92400e;
        margin-bottom: 18px;

        .el-icon {
          font-size: 22px;
        }
      }
    }

    .article-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
    }

    .article-card {
      background: #fff;
      border-radius: 14px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(146, 64, 14, 0.06);
      cursor: pointer;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      display: flex;
      flex-direction: column;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(146, 64, 14, 0.14);
      }

      .cover-wrapper {
        position: relative;
        width: 100%;
        height: 170px;
        background: #fef3c7;
        overflow: hidden;

        .cover {
          width: 100%;
          height: 100%;
        }

        .cover-fallback {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #d6b656;
          font-size: 40px;
        }

        .category-tag {
          position: absolute;
          top: 10px;
          left: 10px;
          backdrop-filter: blur(4px);
        }
      }

      .card-body {
        padding: 16px;
        display: flex;
        flex-direction: column;
        flex: 1;

        .card-title {
          font-size: 17px;
          font-weight: 600;
          color: #1f2937;
          margin: 0 0 8px 0;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .card-summary {
          font-size: 13px;
          color: #6b7280;
          line-height: 1.6;
          margin: 0 0 14px 0;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          overflow: hidden;
          flex: 1;
        }

        .card-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 14px;
          font-size: 12px;
          color: #9ca3af;

          .meta-item {
            display: inline-flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
    }

    // 空状态
    .empty-state {
      text-align: center;
      padding: 60px 20px;
      background: #fff;
      border-radius: 14px;
      box-shadow: 0 2px 12px rgba(146, 64, 14, 0.06);

      .empty-icon {
        width: 160px;
        height: 160px;
        margin-bottom: 12px;
      }

      h3 {
        font-size: 20px;
        color: #92400e;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 14px;
        color: #6b7280;
        margin: 0 0 20px 0;
      }
    }
  }

  // 右侧侧栏
  .care-aside {
    width: 300px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;
    position: sticky;
    top: 20px;

    .aside-card {
      background: #fff;
      border-radius: 14px;
      padding: 20px;
      box-shadow: 0 2px 12px rgba(146, 64, 14, 0.06);

      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
        margin: 0 0 8px 0;
      }

      p {
        font-size: 13px;
        color: #6b7280;
        line-height: 1.7;
        margin: 0;
      }
    }

    .warm-card {
      background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
      text-align: center;

      .aside-icon {
        font-size: 36px;
        color: #f59e0b;
        margin-bottom: 8px;
      }

      h4 {
        color: #92400e;
      }
    }

    .hotline-card {
      border-left: 4px solid #ef4444;

      .hotline-text {
        margin-bottom: 12px;
      }

      .hotline-list {
        list-style: none;
        padding: 0;
        margin: 0 0 12px 0;

        li {
          padding: 8px 0;
          border-bottom: 1px dashed #e5e7eb;
          display: flex;
          flex-direction: column;
          gap: 2px;

          &:last-child {
            border-bottom: none;
          }

          .hotline-name {
            font-size: 12px;
            color: #6b7280;
          }

          .hotline-num {
            font-size: 15px;
            font-weight: 600;
            color: #ef4444;
          }
        }
      }

      .hotline-tip {
        font-size: 12px;
        color: #9ca3af;
        font-style: italic;
      }
    }
  }
}

// 响应式：窄屏单列
@media (max-width: 900px) {
  .care-page {
    .content-wrapper {
      flex-direction: column;

      .article-grid {
        grid-template-columns: 1fr;
      }

      .care-aside {
        width: 100%;
        position: static;
      }
    }
  }
}
</style>
