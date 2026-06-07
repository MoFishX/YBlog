<template>
  <div class="dashboard">
    <div v-if="loading" class="text-center py-16">
      <el-icon class="is-loading" :size="28"><Loading /></el-icon>
      <p class="mt-3 text-sm text-gray-400">加载中...</p>
    </div>

    <div v-else-if="error" class="text-center py-16">
      <p class="text-red-500 mb-4">{{ error }}</p>
      <el-button @click="fetchData">重试</el-button>
    </div>

    <template v-else>
      <el-row :gutter="16" class="mb-6">
        <el-col v-for="card in statCards" :key="card.label" :xs="12" :sm="8" :md="4">
          <StatCard :label="card.label" :value="card.value" :color="card.color">
            <template #icon>
              <component :is="card.icon" />
            </template>
          </StatCard>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="16">
          <el-card>
            <template #header>
              <span class="font-medium">近7天访问趋势</span>
            </template>
            <div ref="chartRef" style="height: 360px"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="8" class="mt-4 lg:mt-0">
          <el-card>
            <template #header>
              <span class="font-medium">今日概览</span>
            </template>
            <div v-if="stats" class="space-y-4">
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-500">今日浏览</span>
                <span class="text-lg font-semibold text-gray-900">{{ stats.todayViews }}</span>
              </div>
              <el-divider class="my-2" />
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-500">今日新用户</span>
                <span class="text-lg font-semibold text-gray-900">{{ stats.todayNewUsers }}</span>
              </div>
              <el-divider class="my-2" />
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-500">今日新文章</span>
                <span class="text-lg font-semibold text-gray-900">{{ stats.todayNewArticles }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Loading, UserFilled, Document, ChatDotRound, View, StarFilled } from '@element-plus/icons-vue'
import { dashboardService } from '@/services/admin/dashboardService'
import StatCard from '@/components/admin/common/StatCard.vue'
import type { DashboardStats, TrendItem } from '@/api/admin/modules/dashboard'

const stats = ref<DashboardStats | null>(null)
const trend = ref<TrendItem[]>([])
const loading = ref(true)
const error = ref('')
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const statCards = computed(() => {
  if (!stats.value) return []
  return [
    { label: '总用户', value: stats.value.userCount, icon: UserFilled, color: '#6366f1' },
    { label: '总文章', value: stats.value.articleCount, icon: Document, color: '#10b981' },
    { label: '总评论', value: stats.value.commentCount, icon: ChatDotRound, color: '#f59e0b' },
    { label: '总点赞', value: stats.value.totalLikes, icon: StarFilled, color: '#8b5cf6' },
    { label: '总浏览量', value: stats.value.totalViews, icon: View, color: '#ef4444' }
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
  } catch (e: any) {
    error.value = e?.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
    await nextTick()
    renderChart()
  }
}

function renderChart() {
  if (!chartRef.value || trend.value.length === 0) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 8, right: 16, top: 16, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: trend.value.map((t) => t.date.length === 10 ? t.date.slice(5) : t.date),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#9ca3af' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#9ca3af' }
    },
    series: [{
      data: trend.value.map((t) => t.count),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(99,102,241,0.08)' },
      lineStyle: { color: '#6366f1', width: 2 },
      itemStyle: { color: '#6366f1' }
    }]
  })
}

onMounted(() => fetchData())

onBeforeUnmount(() => {
  chart?.dispose()
})
</script>
