<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">接口测试执行</span>
      <div>
        <el-select v-model="selectedProject" placeholder="选择项目" style="width:180px;margin-right:12px">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button type="primary" :disabled="selectedCases.length === 0" @click="executeSuite">
          批量执行 ({{ selectedCases.length }})
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" border stripe @selection-change="onSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="name" label="用例名称" min-width="180" />
      <el-table-column prop="method" label="方法" width="80">
        <template #default="{ row }">
          <el-tag :type="methodTag[row.method]" size="small">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="URL" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="success" :loading="executingId === row.id" @click="executeSingle(row.id)">执行</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="resultVisible" title="执行结果" width="900px">
      <div v-if="result">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="用例名称">{{ result.caseName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="result.result === 'PASS' ? 'success' : 'danger'">{{ result.result }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ result.responseTime }} ms</el-descriptions-item>
          <el-descriptions-item label="状态码" :span="3">{{ result.responseStatus }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="activeTab" style="margin-top:16px">
          <el-tab-pane label="请求" name="request">
            <div><strong>URL:</strong> {{ result.requestUrl }}</div>
            <div style="margin-top:8px"><strong>Headers:</strong></div>
            <pre style="background:#f5f7fa;padding:12px;border-radius:4px">{{ formatJson(result.requestHeaders) }}</pre>
            <div style="margin-top:8px"><strong>Body:</strong></div>
            <pre style="background:#f5f7fa;padding:12px;border-radius:4px">{{ result.requestBody }}</pre>
          </el-tab-pane>
          <el-tab-pane label="响应" name="response">
            <pre style="background:#f5f7fa;padding:12px;border-radius:4px;max-height:400px;overflow:auto">{{ formatJson(result.responseBody) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="断言" name="assertions">
            <div v-for="(a, i) in parseAssertions(result.assertions)" :key="i" style="margin-bottom:8px">
              <el-tag :type="a.passed ? 'success' : 'danger'" size="small">{{ a.passed ? '通过' : '失败' }}</el-tag>
              <span style="margin-left:8px">{{ a.field }} {{ a.operator }} {{ a.expected }} (实际: {{ a.actual }})</span>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getApiCasePage, executeApiCase, executeApiSuite, getProjectList } from '@/api'

const selectedProject = ref(null)
const selectedCases = ref([])
const tableData = ref([])
const executingId = ref(null)
const resultVisible = ref(false)
const result = ref(null)
const activeTab = ref('request')
const projects = ref([])

const methodTag = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }

const loadData = async () => {
  const res = await getApiCasePage({ current: 1, size: 100, projectId: selectedProject.value })
  tableData.value = res.data.records
}

const onSelectionChange = (rows) => {
  selectedCases.value = rows
}

const executeSingle = async (id) => {
  executingId.value = id
  try {
    const res = await executeApiCase(id)
    const record = res.data
    const detailRes = await getTestRecordDetail(record.id)
    if (detailRes.data.results && detailRes.data.results.length > 0) {
      result.value = detailRes.data.results[0]
      resultVisible.value = true
    }
    ElMessage.success(record.status === 'PASS' ? '执行通过' : '执行失败')
  } finally {
    executingId.value = null
  }
}

const executeSuite = async () => {
  const caseIds = selectedCases.value.map(c => c.id).join(',')
  const res = await executeApiSuite({ caseIds, projectId: selectedProject.value })
  ElMessage.success(`执行完成: 通过${res.data.passCount}, 失败${res.data.failCount}`)
}

const getTestRecordDetail = (id) => {
  // 复用 api 中的 getTestRecord
  return import('@/api').then(m => m.getTestRecord(id))
}

const formatJson = (str) => {
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

const parseAssertions = (str) => {
  try { return JSON.parse(str) } catch { return [] }
}

onMounted(async () => {
  projects.value = (await getProjectList()).data
  if (projects.value.length > 0) selectedProject.value = projects.value[0].id
  loadData()
})
</script>
