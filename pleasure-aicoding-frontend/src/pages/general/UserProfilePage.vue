<template>
  <div class="user-profile-page">
    <a-card class="profile-card" :loading="loading">
      <!-- 页面标题 -->
      <template #title>
        <div class="page-title">
          <UserOutlined />
          <span>个人中心</span>
        </div>
      </template>
      <a-row :gutter="[24, 24]">
        <!-- 左侧：头像和基本信息 -->
        <a-col :xs="24" :md="8">
          <div class="avatar-section">
            <div class="avatar-container">
              <a-avatar :size="120" :src="userInfo.userAvatar">
                {{ userInfo.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <a-upload
                name="avatar"
                :show-upload-list="false"
                :before-upload="beforeAvatarUpload"
                :custom-request="handleAvatarUpload"
                accept="image/png,image/jpeg,image/jpg"
              >
                <a-button type="primary" size="small" class="upload-btn">
                  <UploadOutlined />
                  更换头像
                </a-button>
              </a-upload>
            </div>
            <!-- 用户基本信息展示 -->
            <div class="basic-info">
              <div class="info-item">
                <span class="label">用户ID：</span>
                <span class="value">{{ userInfo.id }}</span>
              </div>
              <div class="info-item">
                <span class="label">账号：</span>
                <span class="value">{{ userInfo.userAccount }}</span>
              </div>
              <div class="info-item">
                <span class="label">角色：</span>
                <a-tag :color="getRoleColor(userInfo.userRole)">
                  {{ getRoleText(userInfo.userRole) }}
                </a-tag>
              </div>
              <div class="info-item">
                <span class="label">注册时间：</span>
                <span class="value">{{ formatTime(userInfo.createTime) }}</span>
              </div>
            </div>
          </div>
        </a-col>
        <!-- 右侧：可编辑信息 -->
        <a-col :xs="24" :md="16">
          <a-form
            :model="editForm"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 20 }"
            @finish="handleSubmit"
          >
            <!-- 用户名编辑 -->
            <a-form-item
              label="用户名"
              name="userName"
              :rules="[
                { required: true, message: '请输入用户名' },
                { min: 1, max: 20, message: '用户名长度在1-20个字符' }
              ]"
            >
              <a-input
                v-model:value="editForm.userName"
                placeholder="请输入用户名"
                :max-length="20"
                show-count
              />
            </a-form-item>
            <!-- 个人简介编辑 -->
            <a-form-item
              label="个人简介"
              name="userProfile"
              :rules="[
                { max: 200, message: '个人简介不能超过200个字符' }
              ]"
            >
              <a-textarea
                v-model:value="editForm.userProfile"
                placeholder="请输入个人简介"
                :rows="4"
                :max-length="200"
                show-count
              />
            </a-form-item>
            <!-- 操作按钮 -->
            <a-form-item :wrapper-col="{ offset: 4, span: 20 }">
              <a-space>
                <a-button type="primary" html-type="submit" :loading="submitting">
                  <SaveOutlined />
                  保存修改
                </a-button>
                <a-button @click="resetForm">
                  <ReloadOutlined />
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  UploadOutlined,
  SaveOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getLoginUser, updateMyInfo } from '@/api/userController'
import { formatTime } from '@/utils/time'
import { compressAvatar, getBase64Size } from '@/utils/imageCompress'

const loginUserStore = useLoginUserStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const userInfo = ref<API.LoginUserVO>({})
const editForm = reactive<API.UserUpdateRequest>({
  userName: '',
  userProfile: ''
})

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
      // 初始化编辑表单
      editForm.userName = res.data.data.userName || ''
      editForm.userProfile = res.data.data.userProfile || ''
      editForm.id = res.data.data.id
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await updateMyInfo(editForm)
    if (res.data.code === 0) {
      message.success('更新成功')
      // 刷新用户信息
      await fetchUserInfo()
      // 更新全局用户状态
      await loginUserStore.fetchLoginUser()
    } else {
      message.error(res.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    message.error('更新失败')
  } finally {
    submitting.value = false
  }
}

// 合并后的头像上传处理方法
const handleAvatarUpload = async ({ file }: { file: File }) => {
  try {
    const originalSizeKB = (file.size / 1024).toFixed(1)
    message.loading('正在处理图片...', 0)
    let base64: string
    // 判断是否需要压缩（大于500KB的图片进行压缩）
    if (file.size > 500 * 1024) {
      // 智能压缩：根据原始大小调整压缩参数
      let quality = 0.8
      let maxSize = 400
      if (file.size > 2 * 1024 * 1024) { // 大于2MB
        quality = 0.6
        maxSize = 300
      } else if (file.size > 1024 * 1024) { // 大于1MB
        quality = 0.7
        maxSize = 350
      }

      // 压缩图片
      base64 = await compressAvatar(file, {
        quality,
        maxSize,
        outputFormat: 'image/jpeg'
      })

      const compressedSizeKB = (getBase64Size(base64) / 1024).toFixed(1)
      console.log(`图片压缩完成: ${originalSizeKB}KB -> ${compressedSizeKB}KB`)
      message.destroy()
      message.success(`图片已压缩: ${originalSizeKB}KB -> ${compressedSizeKB}KB`)
    } else {
      // 小图片直接转换为base64，不压缩
      base64 = await new Promise<string>((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = (e) => resolve(e.target?.result as string)
        reader.onerror = reject
        reader.readAsDataURL(file)
      })

      message.destroy()
      console.log(`图片无需压缩: ${originalSizeKB}KB`)
    }

    // 更新用户信息
    const updateData = {
      userName: editForm.userName,
      userProfile: editForm.userProfile,
      userAvatar: base64
    }

    const res = await updateMyInfo(updateData)
    if (res.data.code === 0) {
      message.success('头像更新成功')
      await fetchUserInfo()
      await loginUserStore.fetchLoginUser()
    } else {
      message.error('头像更新失败：' + res.data.message)
    }
  } catch (error) {
    message.destroy()
    console.error('头像上传失败:', error)
    message.error('头像上传失败')
  }
}

// 重置表单
const resetForm = () => {
  editForm.userName = userInfo.value.userName || ''
  editForm.userProfile = userInfo.value.userProfile || ''
}

// 头像上传前验证
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }

  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 获取角色颜色
const getRoleColor = (role?: string) => {
  switch (role) {
    case 'admin':
      return 'red'
    case 'user':
      return 'blue'
    default:
      return 'default'
  }
}

// 获取角色文本
const getRoleText = (role?: string) => {
  switch (role) {
    case 'admin':
      return '管理员'
    case 'user':
      return '普通用户'
    default:
      return '未知'
  }
}

// 组件挂载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-profile-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.profile-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.avatar-section {
  text-align: center;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.upload-btn {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.basic-info {
  text-align: left;
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-page {
    padding: 16px;
  }

  .avatar-container {
    margin-bottom: 16px;
  }

  .basic-info {
    margin-top: 16px;
  }
}
</style>
