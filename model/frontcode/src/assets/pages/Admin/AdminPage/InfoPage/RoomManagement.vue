<template>
  <el-card>
    <h3>房间管理</h3>
    <el-table :data="rooms" border v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="房间ID" width="100" align="center"></el-table-column>
      <el-table-column prop="name" label="房间名称" min-width="150"></el-table-column>

      <el-table-column label="房间状态" width="120" align="center">
        <template #default="{row}">
          <el-tag :type="getStatusStyle(row.roomStatus)">
            {{ roomStatusMap[row.roomStatus] || row.roomStatus }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="封禁状态" width="120" align="center">
        <template #default="{row}">
          <el-tag :type="row.isBanned ? 'danger' : 'success'">
            {{ row.isBanned ? '已封禁' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180">
        <template #default="{row}">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button
              size="small"
              :type="row.isBanned ? 'success' : 'danger'"
              @click="toggleBan(row)"
              :disabled="row.roomStatus === 'PRIVATE'"
          >
            {{ row.isBanned ? "解封" : "封禁" }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchRooms"
        class="pagination-wrapper"
    />
  </el-card>
</template>

<script>
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export default {
  data() {
    return {
      rooms: [],
      loading: false,
      page: 1,
      pageSize: 10,
      total: 0,
      roomStatusMap: {
        PUBLIC: '公开',
        SUSPENDED: '已封禁',
        PRIVATE: '私有',
        ENCRYPTED: '加密'
      }
    }
  },
  mounted() {
    this.fetchRooms()
  },
  methods: {
    // 获取房间列表（核心分页方法）
    async fetchRooms() {
      this.loading = true
      try {
        const res = await request.get('/api/master/rooms', {
          params: {
            page: this.page,
            size: this.pageSize
          }
        })

        // 处理不同响应格式
        const dataArray = Array.isArray(res.data) ? res.data : res.data?.rooms || []

        // 字段映射
        this.rooms = dataArray.map(item => ({
          id: item.id,
          name: item.roomName || '未命名房间',
          createdAt: item.createdAt,
          roomStatus: item.roomStatus || 'PUBLIC',
          isBanned: item.banned || false
        }))

        // 分页总数计算（与用户管理完全相同的逻辑）
        if (dataArray.length === this.pageSize) {
          this.total = (this.page + 1) * this.pageSize
        } else {
          this.total = (this.page - 1) * this.pageSize + dataArray.length
        }

      } catch (error) {
        ElMessage.error('获取房间列表失败')
      } finally {
        this.loading = false
      }
    },

    // 状态样式
    getStatusStyle(status) {
      const styleMap = {
        PUBLIC: 'success',
        SUSPENDED: 'danger',
        PRIVATE: 'warning',
        ENCRYPTED: 'info'
      }
      return styleMap[status] || 'default'
    },

    // 封禁/解禁操作
    async toggleBan(room) {
      try {
        const newStatus = !room.isBanned
        await this.$confirm(
            `确定要${newStatus ? '封禁' : '解封'}房间【${room.name}】吗？`,
            '操作确认',
            { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        )

        await request.post(`/api/master/rooms/${room.id}/ban`, { banned: newStatus })

        // 更新本地状态
        const index = this.rooms.findIndex(r => r.id === room.id)
        if (index !== -1) {
          this.rooms.splice(index, 1, { ...room, isBanned: newStatus })
        }

        ElMessage.success(`${newStatus ? '已封禁' : '已解封'}房间【${room.name}】`)
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(`操作失败: ${error.response?.data?.message || '服务器错误'}`)
        }
      }
    },

    // 时间格式化
    formatTime(timestamp) {
      return timestamp ? timestamp.split('.')[0] : 'N/A'
    }
  }
}
</script>