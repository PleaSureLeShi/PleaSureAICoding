<template>
  <el-card class="admin-list">
    <h3 style="margin-bottom: 20px">管理员列表</h3>
    <el-row :gutter="20">
      <el-col
          v-for="admin in admins"
          :key="admin.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="admin-col"
      >
        <el-card shadow="hover" class="admin-card">
          <div class="admin-header">
            <el-avatar
                :src="admin.avatar || defaultAvatar"
                :size="60"
                class="admin-avatar"
            ></el-avatar>
            <div class="admin-basic">
              <h4 class="admin-username">{{ admin.username }}</h4>
              <div class="admin-nickname">{{ admin.nickname || '暂无昵称' }}</div>
            </div>
          </div>

          <div class="admin-info">
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span>{{ admin.email || '暂无邮箱' }}</span>
            </div>

            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ admin.bio || '暂无个人简介' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
import {Message, User} from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/images/SystemLogo.png'

export default {
  data() {
    return {
      admins: [],
      defaultAvatar: defaultAvatar
    }
  },
  mounted() {
    this.fetchAdmins()
  },
  methods: {
    async fetchAdmins() {
      try {
        const res = await request.get('/api/admins')
        this.admins = res.data.admins.map(admin => ({
          ...admin,
          avatar: admin.avatar || this.defaultAvatar
        }))
      } catch (error) {
        ElMessage.error('获取管理员列表失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-list {
  margin: 20px;
}

.admin-col {
  margin-bottom: 20px;
}

.admin-card {
  height: 100%;
  transition: transform 0.3s;
}

.admin-card:hover {
  transform: translateY(-5px);
}

.admin-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.admin-avatar {
  flex-shrink: 0;
  margin-right: 15px;
}

.admin-basic {
  flex-grow: 1;
}

.admin-username {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.admin-nickname {
  font-size: 12px;
  color: #909399;
}

.admin-info {
  font-size: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  margin: 8px 0;
  color: #606266;
}

.info-item .el-icon {
  margin-right: 8px;
  color: #909399;
}

/* 已移除标签相关样式 */
</style>