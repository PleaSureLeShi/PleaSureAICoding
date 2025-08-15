<template>
  <div id="my-page" style="display: flex; flex-direction: column; min-height: 100vh;">
    <!-- 顶部菜单栏 -->
    <el-header id="head">
      <div id="headContent" style="width: 100%; display: flex; align-items: center; justify-content: space-between;">
        <!-- 左侧内容 -->
        <div style="display: flex; align-items: center;">
          <span style="font-size: 1.5em; font-weight: bold;">伙伴匹配平台</span>
          <div style="margin-left: 20px; display: flex;">
            <el-button @click="goToHomePage">首页</el-button>
            <el-button @click="goToFriendPage">好友</el-button>
            <el-button @click="goToRoomPage">房间</el-button>
            <el-button @click="goToMyPage">我的</el-button>
            <!-- 判断角色，显示不同的管理按钮 -->
            <el-button v-if="user.role === 1" @click="goToMasterPage">平台管理</el-button>
            <el-button v-if="user.role === 2" @click="goToAdminPage">平台管理</el-button>
          </div>
        </div>
        <!-- 右侧内容 -->
        <div style="display: flex; align-items: center;">
          <el-dropdown trigger="hover" @command="handleCommand">
            <span class="el-dropdown-link">
              <img :src="user.avatar" alt="User Avatar" style="width: 40px; height: 40px; border-radius: 50%;">
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <p style="text-align: center">昵称: {{user.nickname}}</p>
                <p v-if="user.role === 1" style="text-align: center">当前身份：站长</p>
                <p v-if="user.role === 2" style="text-align: center">当前身份：管理员</p>
                <p v-if="user.role === 3" style="text-align: center">当前身份：普通用户</p>
                <el-dropdown-item divided command="profile">进入个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主体区域 -->
    <el-main style="margin-top: 60px; flex-grow: 1; padding: 20px;">
      <!-- 个人信息 -->
      <el-card class="user-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-avatar :src="user.avatar" :size="100"></el-avatar>
            <el-upload
                :action="`${API_BASE}/api/user/upload-avatar`"
                :headers="{ Authorization: `Bearer ${this.auth.getToken()}` }"
                :on-success="handleAvatarSuccess"
                :http-request="handleCustomUpload"
                :before-upload="beforeAvatarUpload"
            >
              <el-button type="primary" size="mini">更换头像</el-button>
            </el-upload>
          </el-col>
          <el-col :span="18">
            <h2>昵称: {{ user.nickname }}</h2>
            更新昵称: <el-input v-model="user.nickname" placeholder="请输入你想要的新昵称" style="width: 200px; margin-bottom: 10px;"></el-input>
            <p>ID: {{ user.id }}</p>
            <p>用户名: {{ user.username }}</p>
            <p>手机号: {{ user.phone }}</p>
            <p>邮箱: {{ user.email }}</p>
            <el-button type="primary" size="mini" @click="updateNickname">保存昵称</el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 个人简介 -->
      <el-card class="user-bio">
        <h3>个人简介</h3>
        <el-input
            v-model="user.bio"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介"
        ></el-input>
        <el-button type="primary" size="mini" @click="updateBio" style="margin-top: 10px;">保存简介</el-button>
      </el-card>

      <!-- 标签管理 -->
      <el-card class="user-tags">
        <h3>标签</h3>
        <el-select
            v-model="user.tags"
            multiple
            placeholder="请选择标签"
            style="width: 100%;"
        >
          <el-option
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
              :value="tag"
          ></el-option>
        </el-select>
        <el-button type="primary" size="mini" @click="updateTags" style="margin-top: 10px;">保存标签</el-button>
      </el-card>

      <!-- 社交统计 -->
      <el-card class="social-stats">
        <h3>社交统计</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card @click="goToFriendPage" class="stat-card">
              <p>好友数量: {{ user.friendCount }}</p>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card @click="goToRoomPage" class="stat-card">
              <p>加入房间数: {{ user.roomCount }}</p>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <!-- 已创建房间 -->
      <el-card class="created-rooms">
        <h3>已创建房间</h3>
        <el-table :data="createdRooms" border>
          <el-table-column prop="name" label="房间名"></el-table-column>
          <el-table-column prop="userCount" label="成员数量"></el-table-column>
<!--          <el-table-column label="操作">-->
<!--            <template #default="{ row }">-->
<!--              <el-button type="primary" size="mini" @click="enterRoom(row.id)">进入</el-button>-->
<!--            </template>-->
<!--          </el-table-column>-->
        </el-table>
        <el-pagination
            v-model:current-page="createdRoomsPage"
            :total="createdRoomsTotal"
            @current-change="fetchCreatedRooms"
        ></el-pagination>
      </el-card>

      <!-- 已加入房间 -->
      <el-card class="joined-rooms">
        <h3>已加入房间</h3>
        <el-table :data="joinedRooms" border>
          <el-table-column prop="name" label="房间名"></el-table-column>
          <el-table-column prop="userCount" label="成员数量"></el-table-column>
<!--          <el-table-column label="操作">-->
<!--            <template #default="{ row }">-->
<!--              <el-button type="primary" size="mini" @click="enterRoom(row.id)">进入</el-button>-->
<!--            </template>-->
<!--          </el-table-column>-->
        </el-table>
        <el-pagination
            v-model:current-page="joinedRoomsPage"
            :total="joinedRoomsTotal"
            @current-change="fetchJoinedRooms"
        ></el-pagination>
      </el-card>
    </el-main>
  </div>
</template>

<script src="@/assets/styles/MyPage/My.js"></script>
<style scoped src="@/assets/styles/MyPage/My.css"></style>