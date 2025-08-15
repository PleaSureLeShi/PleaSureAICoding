<template>
  <div id="app" style="display: flex; flex-direction: column; min-height: 100vh;">
    <!-- 头部 -->
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

    <!-- 主体 -->
    <el-main style="margin-top: 60px; flex-grow: 1;">
      <el-row id="picture">
        <el-col :span="24">
          <el-carousel :interval="3000" arrow="always">
            <el-carousel-item v-for="(image, index) in images" :key="index">
              <img :src="image" alt="carousel image" style="width: 100%; height: 100%;">
            </el-carousel-item>
          </el-carousel>
        </el-col>
      </el-row>
      <el-row :gutter="20" id="welcome">
        <el-col :span="24">
          <div id="welcomeContent" class="content-container">
            <h1>欢迎来到智能伙伴匹配平台</h1>
            <p>在这里，你可以找到志同道合的伙伴，一起学习、进步、成长！</p>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" id="introduce">
        <el-col :span="8" v-for="(item, index) in introduceItems" :key="index">
          <div class="introduce-container">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" id="encourage">
        <el-col :span="24">
          <div class="daily-box">
            <h3>每日一句</h3>
            <blockquote>
              <p>"{{ randomQuote.quote }}"</p>
              <footer>- {{ randomQuote.author }}</footer>
            </blockquote>
          </div>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script>
import Home from '@/assets/styles/HomePage/Home.js'

export default {
  ...Home, // 将 Home.js 中的内容合并到组件中
};
</script>

<style src="@/assets/styles/HomePage/Home.css"></style>