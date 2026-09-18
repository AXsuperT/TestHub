<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">接口用例管理</span>
      <el-button type="primary" @click="openDialog()">新增接口用例</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="用例名称" clearable style="width:200px" />
      <el-select v-model="query.projectId" placeholder="项目" clearable style="width:160px">
        <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="query.method" placeholder="请求方法" clearable style="width:120px">
        <el-option label="GET" value="GET" />
        <el-option label="POST" value="POST" />
        <el-option label="PUT" value="PUT" />
        <el-option label="DELETE" value="DELETE" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="name" label="用例名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="method" label="方法" width="90">
        <template #default="{ row }">
          <el-tag :type="methodTag[row.method]" size="small">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="请求URL" min-width="220" show-overflow-tooltip />
      <el-table-column prop="bodyType" label="请求体" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="execute(row.id)">执行</el-button>
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
      layout="total, prev, pager, next"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑接口用例' : '新增接口用例'" width="800px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用例名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="请求方法">
              <el-select v-model="form.method" style="width:100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item label="请求URL">
              <el-input v-model="form.url" placeholder="/api/xxx 或 完整URL" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请求头">
          <el-input v-model="form.headers" type="textarea" :rows="2" placeholder='{"Content-Type":"application/json"}' />
        </el-form-item>
        <el-form-item label="请求体类型">
          <el-radio-group v-model="form.bodyType">
            <el-radio-button label="NONE" />
            <el-radio-button label="JSON" />
            <el-radio-button label="FORM" />
            <el-radio-button label="RAW" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="请求体" v-if="form.bodyType !== 'NONE'">
          <el-input v-model="form.body" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="断言规则">
          <el-input v-model="form.assertions" type="textarea" :rows="3"
            placeholder='[{"field":"statusCode","operator":"equals","expected":200}]' />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApiCasePage, saveApiCase, deleteApiCase, getProjectList } from '@/api'

const router = useRouter()
const query = reactive({ current: 1, size: 10, keyword: '', projectId: null, method: '' })
const tableData = ref([])
const total = ref(0)
const projects = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', projectId: null, method: 'GET', url: '', headers: '', bodyType: 'NONE', body: '', assertions: '' })

const methodTag = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }

const loadData = async () => {
  const res = await getApiCasePage(query)
  tableData.value = res.data.records
  total.value = res.data.total
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, name: '', projectId: null, method: 'GET', url: '', headers: '', bodyType: 'NONE', body: '', assertions: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name || !form.url || !form.projectId) {
    ElMessage.warning('请填写必填项')
    return
  }
  await saveApiCase(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await deleteApiCase(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const execute = (id) => {
  router.push(`/api-execute?caseId=${id}`)
}

onMounted(async () => {
  projects.value = (await getProjectList()).data
  loadData()
})
</script>
