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
            <div style="display: inline-flex; gap: 8px">
              <el-button
                  v-if="row.role === 3"
                  size="small"
                  type="primary"
                  @click="setAsAdmin(row)"
                  :disabled="row.role === 1 || row.id === currentUserId"
              >
                设为管理员
              </el-button>

              <el-button
                  v-if="row.role === 2"
                  size="small"
                  type="warning"
                  @click="revokeAdmin(row)"
                  :disabled="row.role === 1 || row.id === currentUserId"
              >
                撤销管理员
              </el-button>
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
        class="pagination-wrapper"
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
        if (user.role == 1) {
          ElMessage.warning('禁止操作站长账号')
          return
        }
        if (user.id == currentUserId) {
          ElMessage.warning('不能操作自己的账号')
          return
        }
        if (currentUserRole != 1) {
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
        console.error('封禁操作失败:', {
          error: error.response?.data || error.message,
          config: error.config
        })
        ElMessage.error(`操作失败: ${error.response?.data?.message || '服务器内部错误'}`)
      }
    },

    // 时间格式化
    formatTime(timestamp) {
      if (!timestamp) return 'N/A'
      const date = new Date(timestamp)
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
    },
    // 设置用户为管理员
    async setAsAdmin(user) {
      try {
        if (!this.validateOperation(user)) return;

        // 严格按照文档使用PUT方法和原始路径
        await request.put(`/api/admin/users/${user.id}/role`,
            { role: 2 },
            {
              headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${auth.getToken()}`
              }
            }
        );

        ElMessage.success(`用户 ${user.username} 已设置为管理员`);
        this.fetchUsers();
      } catch (error) {
        console.error('设置管理员失败:', error);
        ElMessage.error(`操作失败: ${error.response?.data?.message || '服务器错误'}`);
      }
    },

// 撤销管理员权限
    async revokeAdmin(user) {
      try {
        if (!this.validateOperation(user)) return;

        await request.post(`/api/admin/users/${user.id}/revoke-admin`, {}, {
          headers: {
            'Authorization': `Bearer ${auth.getToken()}`
          }
        });

        ElMessage.success(`用户 ${user.username} 的管理员权限已撤销`);
        this.fetchUsers(); // 刷新用户列表
      } catch (error) {
        console.error('撤销管理员失败:', error);
        ElMessage.error(`操作失败: ${error.response?.data?.message || '服务器错误'}`);
      }
    },

// 公共权限验证方法
    validateOperation(user) {
      const currentUser = auth.getUserInfo();

      if (user.role === 1) {
        ElMessage.warning('禁止操作站长账号');
        return false;
      }
      if (user.id === currentUser.id) {
        ElMessage.warning('不能操作自己的账号');
        return false;
      }
      if (currentUser.role !== 1) {
        ElMessage.warning('仅站长可执行此操作');
        return false;
      }
      return true;
    }
  }
}
</script>
