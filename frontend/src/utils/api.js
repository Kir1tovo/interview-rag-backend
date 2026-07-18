import request from './request'

export const skillApi = {
  list: () => request.get('/skills'),
  add: (data) => request.post('/skills', data),
  update: (id, data) => request.put(`/skills/${id}`, data),
  delete: (id) => request.delete(`/skills/${id}`),
  detail: (id) => request.get(`/skills/${id}`),
  common: () => request.get('/skills/common')
}

export const matchApi = {
  analyze: (jdId) => request.post(`/match/analyze/${jdId}`),
  detail: (id) => request.get(`/match/${id}`),
  list: () => request.get('/match/list'),
  listByJd: (jdId) => request.get(`/match/list/${jdId}`),
  delete: (id) => request.delete(`/match/${id}`)
}

export const jdApi = {
  list: (params) => request.get('/jd/list', { params }),
  detail: (id) => request.get(`/jd/${id}`)
}

export const learningPlanApi = {
  generate: (matchId) => request.post(`/learning-plan/generate/${matchId}`),
  regenerate: (planId) => request.post(`/learning-plan/regenerate/${planId}`),
  detail: (planId) => request.get(`/learning-plan/${planId}`),
  list: () => request.get('/learning-plan/list'),
  listByJd: (jdId) => request.get(`/learning-plan/list/${jdId}`),
  delete: (planId) => request.delete(`/learning-plan/${planId}`)
}