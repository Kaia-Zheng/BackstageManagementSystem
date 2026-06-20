<template>
  <div class="dashboard-wrap">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0' }">
          <div class="grid-content grid-con-1">
            <div class="grid-con-icon">
              <el-icon :size="32"><OfficeBuilding /></el-icon>
            </div>
            <div class="grid-cont-right">
              <div class="grid-num">{{ data.clubCount }}</div>
              <div>社团总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0' }">
          <div class="grid-content grid-con-2">
            <div class="grid-con-icon">
              <el-icon :size="32"><Calendar /></el-icon>
            </div>
            <div class="grid-cont-right">
              <div class="grid-num">{{ data.activityCountThisMonth }}</div>
              <div>本月活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0' }">
          <div class="grid-content grid-con-3">
            <div class="grid-con-icon">
              <el-icon :size="32"><Tickets /></el-icon>
            </div>
            <div class="grid-cont-right">
              <div class="grid-num">{{ data.registrationTotalCount }}</div>
              <div>报名总人次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0' }">
          <div class="grid-content grid-con-4">
            <div class="grid-con-icon">
              <el-icon :size="32"><Bell /></el-icon>
            </div>
            <div class="grid-cont-right">
              <div class="grid-num">{{ data.pendingActivityCount }}</div>
              <div>待审核活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域：柱状图 + 饼图 -->
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">近6个月活动发布趋势</span>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">社团类别分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜 + 最新报名 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">热门活动排行 TOP5</span>
          </template>
          <div class="rank-list">
            <div v-for="(item, index) in data.activityRank" :key="item.id" class="rank-item">
              <span class="rank-index" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
              <span class="rank-title">{{ item.title }}</span>
              <span class="rank-count">{{ item.registrationCount }} 人</span>
            </div>
            <el-empty v-if="data.activityRank.length === 0" description="暂无数据" :image-size="60" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">最新报名记录</span>
          </template>
          <el-table :data="data.recentRegistrations" stripe style="width: 100%">
            <el-table-column prop="username" label="报名人" width="120" />
            <el-table-column prop="activityTitle" label="活动" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="regStatusType(row.status)" size="small">{{ regStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="registerTime" label="报名时间" width="170" />
          </el-table>
          <el-empty v-if="data.recentRegistrations.length === 0" description="暂无报名记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue';
import * as echarts from 'echarts';
import { OfficeBuilding, Calendar, Tickets, Bell } from '@element-plus/icons-vue';
import { getDashboard, DashboardData } from '../api/statistics';

const barChartRef = ref<HTMLElement>();
const pieChartRef = ref<HTMLElement>();
let barChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;

const data = reactive<DashboardData>({
  clubCount: 0,
  activityCountThisMonth: 0,
  registrationTotalCount: 0,
  pendingActivityCount: 0,
  monthLabels: [],
  monthActivityCounts: [],
  categoryDistribution: [],
  activityRank: [],
  recentRegistrations: []
});

const regStatusLabel = (s: string) => {
  const map: Record<string, string> = { REGISTERED: '已报名', CHECKED_IN: '已签到', CANCELLED: '已取消' };
  return map[s] || s;
};

const regStatusType = (s: string) => {
  const map: Record<string, string> = { REGISTERED: '', CHECKED_IN: 'success', CANCELLED: 'info' };
  return map[s] || 'info';
};

const initBarChart = () => {
  if (!barChartRef.value) return;
  barChart = echarts.init(barChartRef.value);
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.monthLabels,
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#666' }
    },
    series: [{
      name: '活动数',
      type: 'bar',
      barWidth: '40%',
      data: data.monthActivityCounts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  });
};

const initPieChart = () => {
  if (!pieChartRef.value) return;
  pieChart = echarts.init(pieChartRef.value);
  const pieData = data.categoryDistribution.map(item => ({
    name: item.name,
    value: item.count
  }));
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '0%', left: 'center' },
    series: [{
      name: '社团类别',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: pieData
    }],
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  });
};

const handleResize = () => {
  barChart?.resize();
  pieChart?.resize();
};

const fetchData = async () => {
  try {
    const res = await getDashboard();
    const d = res.data;
    Object.assign(data, d);
    await nextTick();
    initBarChart();
    initPieChart();
  } catch (e) {
    console.error('获取看板数据失败', e);
  }
};

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  barChart?.dispose();
  pieChart?.dispose();
});
</script>

<style scoped lang="scss">
.dashboard-wrap {
  padding: 10px 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.grid-content {
  display: flex;
  align-items: center;
  height: 100px;
}

.grid-con-icon {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.grid-cont-right {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: #999;
}

.grid-num {
  font-size: 28px;
  font-weight: bold;
}

.grid-con-1 .grid-con-icon { background: linear-gradient(135deg, #409eff, #79bbff); }
.grid-con-1 .grid-num { color: #409eff; }

.grid-con-2 .grid-con-icon { background: linear-gradient(135deg, #67c23a, #95d475); }
.grid-con-2 .grid-num { color: #67c23a; }

.grid-con-3 .grid-con-icon { background: linear-gradient(135deg, #e6a23c, #eebe77); }
.grid-con-3 .grid-num { color: #e6a23c; }

.grid-con-4 .grid-con-icon { background: linear-gradient(135deg, #f56c6c, #f89898); }
.grid-con-4 .grid-num { color: #f56c6c; }

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.rank-list {
  .rank-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child { border-bottom: none; }
  }

  .rank-index {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: bold;
    margin-right: 12px;
    background: #f0f0f0;
    color: #999;
    flex-shrink: 0;

    &.rank-1 { background: #f56c6c; color: #fff; }
    &.rank-2 { background: #e6a23c; color: #fff; }
    &.rank-3 { background: #409eff; color: #fff; }
  }

  .rank-title {
    flex: 1;
    font-size: 14px;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .rank-count {
    font-size: 14px;
    color: #409eff;
    font-weight: 600;
    margin-left: 12px;
    flex-shrink: 0;
  }
}
</style>
