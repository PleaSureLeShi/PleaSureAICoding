<template>
  <el-card>
    <h3>用户管理</h3>
    <el-table :data="users" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="username" label="用户名"/>
      <el-table-column prop="nickname" label="昵称"/>
      <el-table-column label="身份" width="120" align="center">
        <template #default="{row}">
          <el-tag :type="roleTypeMap[row.role]">
            {{ formatRole(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180">
        <template #default="{row}">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{row}">
          <el-tag :type="row.status === 0 ? 'danger' : 'success'">
            {{ row.status === 0 ? '封禁' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-tooltip
              :content="row.role === 1 ? '不能操作站长' : row.id === currentUserId ? '不能操作自己' : ''"
              :disabled="!(row.role === 1 || row.id === currentUserId)"
          >
            <div style="display: inline-block">
              <el-button
                  size="small"
                  :type="row.isBanned ? 'success' : 'danger'"
                  @click="toggleBan(row)"
                  :disabled="row.role === 1 || row.id === currentUserId"
              >
                {{ row.isBanned ? '解封' : '封禁' }}
              </el-button>
            </div>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchUsers"
    />
  </el-card>
</template>

<script>
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
import auth from '@/utils/auth'

export default {
  data() {
    return {
      currentUserId: localStorage.getItem('userId') || null,
      users: [],
      loading: false,
      page: 1,
      pageSize: 10,
      total: 0,
      roleTypeMap: {
        1: 'warning',   // 站长
        2: 'success',   // 管理员
        3: 'primary'    // 普通用户
      }
    }
  },
  computed: {
    currentUser() {
      return auth.getUserInfo()
    }
  },
  mounted() {
    this.fetchUsers()
  },
  methods: {
    // 格式化角色显示
    formatRole(role) {
      const roleMap = {
        1: '站长',
        2: '管理员',
        3: '普通用户'
      }
      return roleMap[role] || '未知身份'
    },

    // 获取用户列表
    async fetchUsers() {
      this.loading = true;
      try {
        const res = await request.get('/api/users', {
          params: {
            page: this.page,
            size: this.pageSize
          }
        });
        const dataArray = Array.isArray(res.data) ? res.data : res.data?.users || [];
        // 添加角色字段处理
        this.users = dataArray.map(user => ({
          ...user,
          role: user.role || 3,
          isBanned: user.status === 0,
        }));
        if (dataArray.length === this.pageSize) {
          this.total = (this.page + 1) * this.pageSize;
        } else {
          this.total = (this.page - 1) * this.pageSize + dataArray.length;
        }

      } catch (error) {
        ElMessage.error('获取用户列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 封禁/解封用户
    async toggleBan(user) {
      try {
        const currentUser = auth.getUserInfo()
        const currentUserId = currentUser.id
        const currentUserRole = currentUser.role

        // 增强权限校验
        if (user.role === 1) {
          ElMessage.warning('禁止操作站长账号')
          return
        }
        if (user.id === currentUserId) {
          ElMessage.warning('不能操作自己的账号')
          return
        }
        if (currentUserRole !== 2) {
          ElMessage.warning('权限不足，请联系管理员')
          return
        }

        const newBannedStatus = !user.isBanned
        const response = await request.post(
            `/api/users/${user.id}/ban`,
            newBannedStatus,
            {
              headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.getToken()}`
              }
            }
        )

        ElMessage.success(`用户 ${user.username} 已${newBannedStatus ? '封禁' : '解封'}`)
        // 立即更新本地状态
        user.isBanned = newBannedStatus
        user.status = newBannedStatus ? 0 : 1
        this.users = [...this.users] // 触发视图更新
      } catch (error) {
        // console.error('封禁操作失败:', {
        //   error: error.response?.data || error.message,
        //   config: error.config
        // })
        ElMessage.error(`操作失败: ${error.response?.data?.message || '服务器内部错误'}`)
      }
    },

    // 时间格式化
    formatTime(timestamp) {
      if (!timestamp) return 'N/A'
      const date = new Date(timestamp)
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
    }
  }
}
</script>
