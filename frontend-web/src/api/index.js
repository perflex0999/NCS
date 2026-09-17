import request from './request'

export const login = (phone, code) => request.post('/api/auth/login', { phone, code })

// 充电站
export const stationList = (params) => request.get('/api/admin/station/list', { params })
export const stationCreate = (data) => request.post('/api/admin/station', data)
export const stationUpdate = (id, data) => request.put(`/api/admin/station/${id}`, data)
export const stationDelete = (id) => request.delete(`/api/admin/station/${id}`)

// 充电桩
export const deviceList = (params) => request.get('/api/admin/device/list', { params })
export const deviceCreate = (data) => request.post('/api/admin/device', data)
export const deviceUpdate = (id, data) => request.put(`/api/admin/device/${id}`, data)
export const deviceDelete = (id) => request.delete(`/api/admin/device/${id}`)

// 订单
export const orderList = (params) => request.get('/api/admin/order/list', { params })

// 价格
export const priceList = (params) => request.get('/api/admin/price/list', { params })
export const priceCreate = (data) => request.post('/api/admin/price', data)
export const priceUpdate = (id, data) => request.put(`/api/admin/price/${id}`, data)
export const priceDelete = (id) => request.delete(`/api/admin/price/${id}`)

// 故障
export const faultList = (params) => request.get('/api/admin/fault/list', { params })
export const faultCreate = (data) => request.post('/api/admin/fault', data)
export const faultUpdate = (id, data) => request.put(`/api/admin/fault/${id}`, data)
export const faultDelete = (id) => request.delete(`/api/admin/fault/${id}`)

// 统计
export const statsOverview = () => request.get('/api/admin/stats/overview')
export const statsTrend = (days = 7) => request.get('/api/admin/stats/trend', { params: { days } })

// AI 助手
export const agentHistory = (scenario) => request.get('/api/agent/history', { params: { scenario } })
export const clearAgentHistory = (scenario) => request.delete('/api/agent/history', { params: { scenario } })
