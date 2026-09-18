import request from '@/utils/request'

// 认证
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')

// 项目
export const getProjectPage = (params) => request.get('/projects/page', { params })
export const getProjectList = () => request.get('/projects/list')
export const saveProject = (data) => request.post('/projects', data)
export const deleteProject = (id) => request.delete(`/projects/${id}`)

// 测试用例
export const getTestCasePage = (params) => request.get('/test-cases/page', { params })
export const getTestCase = (id) => request.get(`/test-cases/${id}`)
export const saveTestCase = (data) => request.post('/test-cases', data)
export const deleteTestCase = (id) => request.delete(`/test-cases/${id}`)

// 接口用例
export const getApiCasePage = (params) => request.get('/api-cases/page', { params })
export const getApiCase = (id) => request.get(`/api-cases/${id}`)
export const saveApiCase = (data) => request.post('/api-cases', data)
export const deleteApiCase = (id) => request.delete(`/api-cases/${id}`)

// 接口测试执行
export const executeApiCase = (caseId) => request.post(`/api-tests/execute/${caseId}`)
export const executeApiSuite = (data) => request.post('/api-tests/execute-suite', data)
export const getTestRecord = (recordId) => request.get(`/api-tests/records/${recordId}`)

// 缺陷
export const getBugPage = (params) => request.get('/bugs/page', { params })
export const getBug = (id) => request.get(`/bugs/${id}`)
export const saveBug = (data) => request.post('/bugs', data)
export const deleteBug = (id) => request.delete(`/bugs/${id}`)
export const changeBugStatus = (id, data) => request.put(`/bugs/${id}/status`, data)

// AI
export const aiChat = (data) => request.post('/ai/chat', data)
export const aiGenerateCases = (data) => request.post('/ai/generate-cases', data)
export const aiAnalyzeBug = (data) => request.post('/ai/analyze-bug', data)
export const aiGenerateApiCases = (data) => request.post('/ai/generate-api-cases', data)
export const aiSummarizeReport = (data) => request.post('/ai/summarize-report', data)
