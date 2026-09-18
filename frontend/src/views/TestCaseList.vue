<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">测试用例管理</span>
      <el-button type="primary" @click="openDialog()">新增用例</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="用例标题" clearable style="width:200px" />
      <el-select v-model="query.projectId" placeholder="项目" clearable style="width:160px">
        <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="query.caseType" placeholder="用例类型" clearable style="width:140px">
        <el-option label="功能" value="FUNCTIONAL" />
        <el-option label="接口" value="API" />
        <el-option label="UI" value="UI" />
        <el-option label="性能" value="PERFORMANCE" />
      </el-select>
      <el-select v-model="query.priority" placeholder="优先级" clearable style="width:120px">
        <el-option label="P0" value="P0" />
        <el-option label="P1" value="P1" />
        <el-option label="P2" value="P2" />
        <el-option label="P3" value="P3" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="caseNo" label="用例编号" width="120" />
      <el-table-column prop="title" label="用例标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="caseType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ typeMap[row.caseType] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="priorityTag[row.priority]" size="small">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top:16px;justify-content:flex-end;display:flex"
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10,20,50]"
      layout="total, sizes, prev, pager, next"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用例' : '新增用例'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用例标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-select v-model="form.projectId" style="width:100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用例类型">
              <el-select v-model="form.caseType" style="width:100%">
                <el-option label="功能" value="FUNCTIONAL" />
                <el-option label="接口" value="API" />
                <el-option label="UI" value="UI" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        <el-form-item label="前置条件">
          <el-input v-model="form.precondition" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="测试步骤">
          <el-input v-model="form.steps" type="textarea" :rows="4" placeholder='JSON数组，如 [{"step":"步骤1","data":"数据"}]' />
        </el-form-item>
        <el-form-item label="预期结果">
          <el-input v-model="form.expected" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTestCasePage, saveTestCase, deleteTestCase, getProjectList } from '@/api'

const query = reactive({ current: 1, size: 10, keyword: '', projectId: null, caseType: '', priority: '' })
const tableData = ref([])
const total = ref(0)
const projects = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', projectId: null, caseType: 'FUNCTIONAL', priority: 'P2', precondition: '', steps: '', expected: '' })

const typeMap = { FUNCTIONAL: '功能', API: '接口', UI: '界面', PERFORMANCE: '性能', SECURITY: '安全' }
const priorityTag = { P0: 'danger', P1: 'warning', P2: '', P3: 'info' }
const statusMap = { DRAFT: '草稿', REVIEW: '待评审', APPROVED: '已评审', OBSOLETE: '废弃' }
const statusTag = { DRAFT: 'info', REVIEW: 'warning', APPROVED: 'success', OBSOLETE: 'danger' }

const loadData = async () => {
  const res = await getTestCasePage(query)
  tableData.value = res.data.records
  total.value = res.data.total
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, title: '', projectId: null, caseType: 'FUNCTIONAL', priority: 'P2', precondition: '', steps: '', expected: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.title || !form.projectId) {
    ElMessage.warning('请填写必填项')
    return
  }
  await saveTestCase(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该用例?', '提示', { type: 'warning' }).then(async () => {
    await deleteTestCase(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(async () => {
  projects.value = (await getProjectList()).data
  loadData()
})
</script>
