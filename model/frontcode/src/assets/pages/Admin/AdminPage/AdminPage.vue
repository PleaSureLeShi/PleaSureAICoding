<template>
  <div id="admin-page" style="display: flex; flex-direction: column; min-height: 100vh;">
    <!-- 顶部菜单栏 -->
    <el-header id="head">
      <div id="headContent" style="width: 100%; display: flex; align-items: center; justify-content: space-between;">
        <!-- 左侧内容 -->
        <div style="display: flex; align-items: center;">
          <span style="font-size: 1.5em; font-weight: bold;">伙伴平台</span>
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
                <p v-if="user.role === 2" style="text-align: center">当前身份：管理员</p>
                <el-dropdown-item divided command="profile">进入个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主体区域 -->
    <el-container style="margin-top: 60px; flex-grow: 1;">
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <el-menu @select="handleMenuSelect">
          <el-menu-item index="platform">平台信息</el-menu-item>
          <el-menu-item index="users">用户管理</el-menu-item>
          <el-menu-item index="rooms">房间管理</el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主体内容 -->
      <el-main id="mainPart">
        <component :is="currentView" />
      </el-main>
    </el-container>
  </div>
</template>

<script src="@/assets/styles/Admin/AdminPage/Admin.js"></script>
<style src="@/assets/styles/Admin/AdminPage/Admin.css"></style>