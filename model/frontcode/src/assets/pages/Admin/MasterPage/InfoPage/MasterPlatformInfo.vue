<template>
  <div>
    <el-card>
      <h3>平台基本信息</h3>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <div class="stat-item">
              <el-icon><User /></el-icon>
              <div class="stat-content">
                <p class="label">用户总数</p>
                <p class="value">{{ data.userCount }}</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <div class="stat-item">
              <el-icon><Opportunity /></el-icon>
              <div class="stat-content">
                <p class="label">活跃用户（7天）</p>
                <p class="value">{{ data.activeUsers }}</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card>
            <div class="stat-item">
              <el-icon><OfficeBuilding /></el-icon>
              <div class="stat-content">
                <p class="label">房间总数</p>
                <p class="value">{{ data.roomCount }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  User,
  Opportunity,
  OfficeBuilding
} from '@element-plus/icons-vue'

export default {
  components: {
    User,
    Opportunity,
    OfficeBuilding
  },
  data() {
    return {
      data: {
        userCount: 0,
        activeUsers: 0,
        roomCount: 0,
      },
      loading: false
    }
  },
  mounted() {
    this.fetchData()
    // 每5分钟自动刷新数据
    this.timer = setInterval(this.fetchData, 300000)
  },
  beforeUnmount() {
    clearInterval(this.timer)
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await request.get('/api/master/platform-info')

        // 数据校验
        if (!res.data || typeof res.data !== 'object') {
          throw new Error('无效的接口响应格式')
        }

        this.data = {
          userCount: res.data.userCount ?? 0,
          activeUsers: res.data.activeUsers ?? 0,
          roomCount: res.data.roomCount ?? 0
        }

      } catch (error) {
        console.error('[平台信息] 获取失败:', {
          error: error.message,
          stack: error.stack
        })

        ElMessage.error({
          message: `获取平台信息失败: ${error.response?.data?.message || error.message}`,
          duration: 5000
        })

        // 失败后重试
        setTimeout(this.fetchData, 5000)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-item .el-icon {
  font-size: 40px;
  margin-right: 20px;
  color: var(--el-color-primary);
}

.stat-content {
  flex: 1;
}

.stat-content .label {
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  font-size: 14px;
}

.stat-content .value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.el-card {
  margin-bottom: 20px;
}

.el-col {
  margin-bottom: 20px;
}
</style>