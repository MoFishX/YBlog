<template>
  <div class="dashboard">
    <div v-if="loading" class="text-center py-12">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p class="mt-2 text-gray-500">加载中...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <p class="text-red-500 mb-4">{{ error }}</p>
      <el-button @click="fetchData">重试</el-button>
    </div>

    <template v-else>
      <el-row :gutter="16" class="mb-6">
        <el-col v-for="card in statCards" :key="card.label" :span="6">
          <StatCard :label="card.label" :value="card.value" :icon="card.icon" :color="card.color" />
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="16">
          <el-card>
            <template #header>近7天访问趋势</template>
            <div ref="chartRef" style="height: 360px"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>今日概览</template>
            <div class="space-y-4" v-if="stats">
              <div class="flex justify-between">
                <span class="text-gray-500">今日访问</span>
                <span class="font-semibold">{{ stats.todayViews }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">今日新用户</span>
                <span class="font-semibold">{{ stats.todayNewUsers }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">今日新文章</span>
                <span class="font-semibold">{{ stats.todayNewArticles }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { Loading } from '@element-plus/icons-vue'
import { dashboardService } from '@/services/dashboardService'
import StatCard from '@/components/common/StatCard.vue'
import type { DashboardStats, TrendItem } from '@/api/modules/dashboard'

const stats = ref<DashboardStats | null>(null)
const trend = ref<TrendItem[]>([])
const loading = ref(true)
const error = ref('')
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const statCards = computed(() => {
  if (!stats.value) return []
  return [
    { label: '总用户', value: stats.value.userCount, icon: 'U', color: '#409EFF' },
    { label: '总文章', value: stats.value.articleCount, icon: 'A', color: '#67C23A' },
    { label: '总评论', value: stats.value.commentCount, icon: 'C', color: '#E6A23C' },
    { label: '总浏览量', value: stats.value.totalViews, icon: 'V', color: '#F56C6C' }
  ]
})

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const [s, t] = await Promise.all([
      dashboardService.getStats(),
      dashboardService.getWeeklyTrend()
    ])
    stats.value = s
    trend.value = t
    renderChart()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || trend.value.length === 0) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trend.value.map((t) => t.date.slice(5))
    },
    yAxis: { type: 'value' },
    series: [{
      data: trend.value.map((t) => t.count),
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(64,158,255,0.15)' },
      lineStyle: { color: '#409EFF' },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

onMounted(() => fetchData())

onBeforeUnmount(() => {
  chart?.dispose()
})
</script>
