import service from '@/utils/request'

/**
 * 分页查询预警列表（管理员）
 * @param {Object} params - { currentPage, size, level?, status? }
 */
export function getAlertPage(params) {
    return service.get('/alert/page', { params })
}

/**
 * 获取预警详情（管理员）
 * @param {Number} id 预警ID
 */
export function getAlertDetail(id) {
    return service.get(`/alert/${id}`)
}

/**
 * 处理预警（管理员）
 * @param {Number} id 预警ID
 * @param {Number} status 目标状态：1已处理 2已忽略
 */
export function handleAlert(id, status) {
    return service.put(`/alert/${id}/handle`, null, { params: { status } })
}

/**
 * 手动触发扫描（管理员，演示用）
 * @param {String} date 目标日期 YYYY-MM-DD，不传默认昨天
 */
export function triggerScan(date) {
    return service.post('/alert/scan/trigger', null, { params: { date } })
}

/**
 * 获取统计数据（管理员）
 */
export function getAlertStats() {
    return service.get('/alert/stats')
}

/**
 * 获取自己的关怀推荐文章（用户）
 */
export function getMyRecommendedArticles() {
    return service.get('/alert/my')
}
