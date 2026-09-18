<template>
  <div class="page-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">测试用例总数</div>
          <div class="stat-value" style="color:#409eff">{{ stats.caseCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">接口用例数</div>
          <div class="stat-value" style="color:#67c23a">{{ stats.apiCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">缺陷总数</div>
          <div class="stat-value" style="color:#e6a23c">{{ stats.bugCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">待处理缺陷</div>
          <div class="stat-value" style="color:#f56c6c">{{ stats.openBugCount }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <div class="stat-card">
          <h3 style="margin-bottom:16px">缺陷严重程度分布</h3>
          <div ref="severityChart" style="height:300px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card">
          <h3 style="margin-bottom:16px">用例类型分布</h3>
          <div ref="typeChart" style="height:300px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="24">
        <div class="stat-card">
          <h3 style="margin-bottom:16px">最近测试执行趋势</h3>
          <div ref="trendChart" style="height:320px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const stats = ref({ caseCount: 128, apiCount: 56, bugCount: 42, openBugCount: 8 })
const severityChart = ref()
const typeChart = ref()
const trendChart = ref()

onMounted(() => {
  nextTick(() => {
    initSeverityChart()
    initTypeChart()
    initTrendChart()
  })
})

const initSeverityChart = () => {
  const chart = echarts.init(severityChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 2, name: '致命 BLOCKER', itemStyle: { color: '#f56c6c' } },
        { value: 8, name: '严重 CRITICAL', itemStyle: { color: '#e6a23c' } },
        { value: 18, name: '一般 MAJOR', itemStyle: { color: '#409eff' } },
        { value: 10, name: '轻微 MINOR', itemStyle: { color: '#67c23a' } },
        { value: 4, name: '建议 TRIVIAL', itemStyle: { color: '#909399' } }
      ],
      label: { formatter: '{b}: {c} ({d}%)' }
    }]
  })
}

const initTypeChart = () => {
  const chart = echarts.init(typeChart.value)
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: ['功能', '接口', 'UI', '性能', '安全'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [68, 56, 32, 18, 12],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 1, color: '#188df0' }
        ]),
        borderRadius: [6, 6, 0, 0]
      }
    }]
  })
}

const initTrendChart = () => {
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['通过', '失败'] },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [
      { name: '通过', type: 'line', smooth: true, data: [42, 55, 38, 60, 72, 45, 33], itemStyle: { color: '#67c23a' }, areaStyle: { opacity: 0.2 } },
      { name: '失败', type: 'line', smooth: true, data: [8, 5, 12, 6, 4, 9, 7], itemStyle: { color: '#f56c6c' }, areaStyle: { opacity: 0.2 } }
    ]
  })
}
</script>

<style scoped>
.stat-row { margin-bottom: 0; }
</style>
