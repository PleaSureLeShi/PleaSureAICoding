<template>
  <el-card>
    <h3>管理员列表</h3>
    <el-table :data="admins" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="头像" width="100">
        <template #default="{ row }">
          <el-avatar :src="row.avatar || defaultAvatar"></el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="nickname" label="用户昵称"></el-table-column>
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          {{ formatRole(row.role) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button
              v-if="row.role === 2"
              type="danger"
              size="small"
              @click="handleRevoke(row)"
              :loading="revokeLoading[row.id]"
          >
            撤销管理员
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchAdmins"
    />
  </el-card>
</template>

<script>
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import defaultAvatar from '@/assets/images/SystemLogo.png'

export default {
  data() {
    return {
      admins: [],
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      revokeLoading: {},  // 改为普通对象即可
      defaultAvatar
    }
  },
  methods: {
    // 格式化角色显示
    formatRole(role) {
      const roleMap = {
        1: '站长',
        2: '管理员',
        3: '普通用户'
      }
      return roleMap[role] || '未知角色'
    },

    // 获取管理员列表
    async fetchAdmins() {
      this.loading = true
      try {
        const res = await request.get('/api/admins', {
          params: {
            page: this.page,
            pageSize: this.pageSize
          }
        })

        this.admins = res.data.admins.map(user => ({
          ...user,
          avatar: this.formatAvatar(user.avatar)
        }))
        this.total = res.data.total
      } catch (error) {
        ElMessage.error('获取管理员列表失败')
      } finally {
        this.loading = false
      }
    },

    // 处理头像地址
    formatAvatar(avatar) {
      if (!avatar) return ''
      return avatar.startsWith('http') ? avatar : `${process.env.VUE_APP_BASE_URL}${avatar}`
    },

    // 撤销管理员权限
    async handleRevoke(user) {
      try {
        await ElMessageBox.confirm(
            `确定要撤销 ${user.nickname || user.username} 的管理员权限吗？`,
            '操作确认',
            { confirmButtonText: '确定', cancelButtonText: '取消' }
        )
        this.revokeLoading[user.id] = true
        await request.post(`/api/admins/${user.id}/revoke`)
        ElMessage.success('权限撤销成功')
        this.fetchAdmins()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.response?.data?.message || '操作失败')
        }
      } finally {
        this.revokeLoading[user.id] = false
      }
    }
  },
  mounted() {
    this.fetchAdmins()
  }
}
</script>

<style scoped>
.el-card {
  margin: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>