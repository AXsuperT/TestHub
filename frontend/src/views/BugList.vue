<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">缺陷管理</span>
      <el-button type="primary" @click="openDialog()">提交缺陷</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="缺陷标题" clearable style="width:200px" />
      <el-select v-model="query.projectId" placeholder="项目" clearable style="width:160px">
        <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
        <el-option label="待处理" value="OPEN" />
        <el-option label="已分配" value="ASSIGNED" />
        <el-option label="已修复" value="FIXED" />
        <el-option label="已验证" value="VERIFIED" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-select v-model="query.severity" placeholder="严重程度" clearable style="width:140px">
        <el-option label="致命" value="BLOCKER" />
        <el-option label="严重" value="CRITICAL" />
        <el-option label="一般" value="MAJOR" />
        <el-option label="轻微" value="MINOR" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="bugNo" label="编号" width="120" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="severity" label="严重程度" width="100">
        <template #default="{ row }">
          <el-tag :type="severityTag[row.severity]" size="small">{{ severityMap[row.severity] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="openStatusDialog(row)">流转</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top:16px;justify-content:flex-end;display:flex"
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑缺陷' : '提交缺陷'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="缺陷标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="所属项目">
              <el-select v-model="form.projectId" style="width:100%">
                <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="严重程度">
              <el-select v-model="form.severity" style="width:100%">
                <el-option label="致命" value="BLOCKER" />
                <el-option label="严重" value="CRITICAL" />
                <el-option label="一般" value="MAJOR" />
                <el-option label="轻微" value="MINOR" />
                <el-option label="建议" value="TRIVIAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" style="width:100%">
                <el-option label="P0" value="P0" />
                <el-option label="P1" value="P1" />
                <el-option label="P2" value="P2" />
                <el-option label="P3" value="P3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="缺陷类型">
          <el-select v-model="form.bugType" style="width:100%">
            <el-option label="功能" value="FUNCTION" />
            <el-option label="数据" value="DATA" />
            <el-option label="UI" value="UI" />
            <el-option label="性能" value="PERFORMANCE" />
            <el-option label="安全" value="SECURITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="缺陷描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="复现步骤">
          <el-input v-model="form.reproduceSteps" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="预期结果">
          <el-input v-model="form.expectedResult" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="实际结果">
          <el-input v-model="form.actualResult" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="缺陷状态流转" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag>{{ statusMap[currentBug.status] }}</el-tag>
        </el-form-item>
        <el-form-item label="流转到">
          <el-select v-model="newStatus" style="width:100%">
            <el-option label="已分配" value="ASSIGNED" />
            <el-option label="已修复" value="FIXED" />
            <el-option label="已验证" value="VERIFIED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="重新打开" value="REOPENED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusChange">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBugPage, saveBug, deleteBug, changeBugStatus, getProjectList } from '@/api'

const query = reactive({ current: 1, size: 10, keyword: '', projectId: null, status: '', severity: '' })
const tableData = ref([])
const total = ref(0)
const projects = ref([])
const dialogVisible = ref(false)
const statusVisible = ref(false)
const currentBug = ref({})
const newStatus = ref('')

const form = reactive({ id: null, title: '', projectId: null, severity: 'MAJOR', priority: 'P2', bugType: 'FUNCTION', description: '', reproduceSteps: '', expectedResult: '', actualResult: '' })

const severityMap = { BLOCKER: '致命', CRITICAL: '严重', MAJOR: '一般', MINOR: '轻微', TRIVIAL: '建议' }
const severityTag = { BLOCKER: 'danger', CRITICAL: 'warning', MAJOR: '', MINOR: 'success', TRIVIAL: 'info' }
const statusMap = { OPEN: '待处理', ASSIGNED: '已分配', FIXED: '已修复', VERIFIED: '已验证', CLOSED: '已关闭', REOPENED: '重新打开' }
const statusTag = { OPEN: 'danger', ASSIGNED: 'warning', FIXED: '', VERIFIED: 'success', CLOSED: 'info', REOPENED: 'warning' }

const loadData = async () => {
  const res = await getBugPage(query)
  tableData.value = res.data.records
  total.value = res.data.total
}

const openDialog = (row) => {
  if (row) { Object.assign(form, row) } else {
    Object.assign(form, { id: null, title: '', projectId: null, severity: 'MAJOR', priority: 'P2', bugType: 'FUNCTION', description: '', reproduceSteps: '', expectedResult: '', actualResult: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.title || !form.projectId) { ElMessage.warning('请填写必填项'); return }
  await saveBug(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await deleteBug(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const openStatusDialog = (row) => {
  currentBug.value = row
  newStatus.value = ''
  statusVisible.value = true
}

const handleStatusChange = async () => {
  await changeBugStatus(currentBug.value.id, { status: newStatus.value })
  ElMessage.success('状态已更新')
  statusVisible.value = false
  loadData()
}

onMounted(async () => {
  projects.value = (await getProjectList()).data
  loadData()
})
</script>
