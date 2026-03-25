<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const stats = ref([
  { title: '总用户数', value: '0', unit: '' },
  { title: '认证用户', value: '0', unit: '' },
  { title: '在售教材', value: '0', unit: '' },
  { title: '总交易额', value: '¥0.00', unit: '' },
])

const chartRef = ref<HTMLElement | null>(null)
let myChart: echarts.ECharts | null = null

const initChart = () => {
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value)
    const option = {
      title: {
        text: '近七日交易趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: {
        type: 'value',
        name: '交易额 (元)'
      },
      series: [
        {
          name: '交易额',
          data: [820, 932, 901, 934, 1290, 1330, 1320],
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.5)' },
              { offset: 1, color: 'rgba(64,158,255,0.1)' }
            ])
          }
        }
      ]
    }
    myChart.setOption(option)
  }
}

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/stats/dashboard')
    if (res.data.code === 200) {
      const data = res.data.data
      stats.value[0].value = data.totalUsers.toString()
      stats.value[1].value = '0' // Backend doesn't provide this yet, placeholder or could fetch users count
      stats.value[2].value = data.activeTextbooks.toString()
      stats.value[3].value = '¥' + data.totalVolume.toFixed(2)
      
      // Optional: Fetch users to count verified if needed
      try {
        const userRes = await axios.get('/api/users')
        if (userRes.data.code === 200) {
           const users = userRes.data.data
           const verifiedCount = users.filter((u: any) => u.verified).length
           stats.value[1].value = verifiedCount.toString()
        }
      } catch (e) {
        console.error('Failed to fetch verified users count', e)
      }
    }
  } catch (error) {
    console.error('Failed to fetch stats', error)
  }
}

const fetchDailySales = async () => {
  try {
    const res = await axios.get('/api/stats/daily-sales')
    if (res.data.code === 200) {
      const { dates, values } = res.data.data
      if (myChart) {
        myChart.setOption({
          xAxis: {
            data: dates
          },
          series: [
            {
              data: values
            }
          ]
        })
      }
    }
  } catch (error) {
    console.error('Failed to fetch daily sales', error)
  }
}

const handleResize = () => {
  myChart?.resize()
}

onMounted(() => {
  fetchStats()
  nextTick(() => {
    initChart()
    fetchDailySales()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart?.dispose()
})
</script>

<template>
  <div class="dashboard-container">
    <h2 class="page-title">数据概览</h2>
    
    <div class="stats-grid">
      <div v-for="(stat, index) in stats" :key="index" class="stat-card">
        <div class="stat-title">{{ stat.title }}</div>
        <div class="stat-value">{{ stat.value }}</div>
      </div>
    </div>

    <div class="chart-container">
      <div ref="chartRef" class="chart"></div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  height: 400px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.chart {
  width: 100%;
  height: 100%;
}
</style>