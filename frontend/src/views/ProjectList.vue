<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">项目管理</span>
      <el-button type="primary" @click="openDialog()">新增项目</el-button>
    </div>

    <el-table :data="tableData" border stripe>
      <el-table-column prop="name" label="项目名称" min-width="180" />
      <el-table-column prop="code" label="项目编码" width="140" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.status === 'ACTIVE' ? '活跃' : '归档' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑项目' : '新增项目'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="项目编码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { getProjectList, saveProject, deleteProject } from '@/api'

const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', code: '', description: '' })

const loadData = async () => {
  const res = await getProjectList()
  tableData.value = res.data
}

const openDialog = (row) => {
  if (row) { Object.assign(form, row) } else {
    Object.assign(form, { id: null, name: '', code: '', description: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name || !form.code) { ElMessage.warning('请填写必填项'); return }
  await saveProject(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await deleteProject(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(loadData)
</script>
