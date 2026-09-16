import request from './request'

export const login = (phone, code) => request.post('/api/auth/login', { phone, code })
export const nearbyStations = (params) => request.get('/api/station/nearby', { params })
export const stationDetail = (id) => request.get(`/api/station/${id}`)
export const startCharge = (deviceNo) => request.post('/api/charge/start', { deviceNo })
export const currentCharge = () => request.get('/api/charge/current')
export const endCharge = () => request.post('/api/charge/end')
export const myOrders = () => request.get('/api/order/my')
