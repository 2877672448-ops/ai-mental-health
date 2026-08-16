<template>
  <div class="markdown-renderer" v-html="renderedContent"></div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  isAiMessage: {
    type: Boolean,
    default: false
  }
})

const escapeHtml = (text) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

const renderMarkdown = (text) => {
  if (!text) return ''
  let html = escapeHtml(text)
  // 加粗 **text**
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // 斜体 *text*
  html = html.replace(/\*([^*]+?)\*/g, '<em>$1</em>')
  // 标题 ### 
  html = html.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^# (.+)$/gm, '<h2>$1</h2>')
  // 无序列表
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.+?<\/li>)(\n|$)/gs, (match) => {
    return match.replace(/(<li>.*?<\/li>)/gs, '$1').replace(/^/, '<ul>').replace(/$/, '</ul>')
  })
  // 有序列表 1. 
  html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
  // 行内代码 `code`
  html = html.replace(/`([^`]+?)`/g, '<code>$1</code>')
  // 换行
  html = html.replace(/\n/g, '<br>')
  return html
}

const renderedContent = computed(() => {
  return renderMarkdown(props.content)
})
</script>

<style scoped lang="scss">
.markdown-renderer {
  line-height: 1.7;
  color: #374151;
  word-break: break-word;

  :deep(h2), :deep(h3), :deep(h4) {
    margin: 12px 0 8px;
    font-weight: 600;
    color: #1f2937;
  }

  :deep(h2) { font-size: 18px; }
  :deep(h3) { font-size: 16px; }
  :deep(h4) { font-size: 15px; }

  :deep(p) {
    margin: 0 0 8px;
  }

  :deep(strong) {
    color: #fb923c;
    font-weight: 600;
  }

  :deep(em) {
    color: #6b7280;
    font-style: italic;
  }

  :deep(ul) {
    padding-left: 20px;
    margin: 8px 0;
    li {
      margin-bottom: 4px;
      list-style: disc;
    }
  }

  :deep(code) {
    background: #f3f4f6;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Consolas', monospace;
    font-size: 13px;
    color: #dc2626;
  }
}
</style>
