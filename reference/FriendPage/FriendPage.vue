<template>
  <div id="app" style="display: flex; flex-direction: column; min-height: 100vh;">
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

    <!-- 主体内容 -->
    <el-main style="margin-top: 60px; flex-grow: 1;">
      <div style="display: flex; height: 100%;">
        <!-- 左侧好友列表 -->
        <div style="width: 30%; background-color: #f5f7fa; padding: 20px; overflow-y: auto;">
          <h2>好友列表</h2>
          <div style="display: flex; gap: 10px;">
            <el-button type="success" @click="showAIDialog">AI 助手</el-button>
          </div>
          <el-divider/>

          <!-- 好友请求 -->
          <h3>好友请求</h3>
          <div v-if="friendRequests.length > 0">
            <el-table :data="friendRequests" style="width: 100%">
              <el-table-column prop="nickname" label="昵称"/>
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button type="success" @click="acceptFriendRequest(row)">同意</el-button>
                  <el-button type="danger" @click="rejectFriendRequest(row)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="text-align: center; color: #999;">
            <p>暂时没有好友请求~</p>
          </div>

          <!-- 已添加好友 -->
          <h3 style="margin-top: 20px;">已添加好友</h3>
          <div v-if="friends.length > 0">
            <el-table :data="friends" style="width: 100%">
              <el-table-column prop="nickname" label="昵称"/>
              <el-table-column prop="tags" label="标签"/>
              <el-table-column label="操作">
                <template #default="{ row }">
                  <el-button @click="viewFriendProfile(row)">开始聊天</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="text-align: center; color: #999;">
            <p>快去添加好友吧！</p>
          </div>
        </div>

        <!-- 右侧好友详情 -->
        <div style="width: 70%; padding: 20px;">
          <div v-if="selectedFriend">
            <h2>{{ selectedFriend.nickname }}</h2>
            <el-avatar :src="selectedFriend.avatar" :size="100"/>
            <p>标签: {{ selectedFriend.tags.join(', ') || '暂无标签' }}</p>
            <p>个人简介: {{ selectedFriend.bio || '暂无简介' }}</p>
            <el-divider/>

            <!-- 好友聊天 -->
            <h3>好友聊天</h3>
            <div style="border: 1px solid #ddd; padding: 10px; height: 300px; overflow-y: auto;">
              <div v-for="message in chatMessages" :key="message.id" style="margin-bottom: 10px;">
                <strong>{{ message.sender_name }}:</strong> {{ message.content }}
              </div>
            </div>
            <el-input
                v-model="newMessage"
                placeholder="输入消息"
                style="margin-top: 10px;"
                @keyup.enter="sendMessage"
            />
          </div>

          <!-- 未选择好友时的提示 -->
          <div v-else style="text-align: center; margin-top: 100px;">
            <el-icon :size="50">
              <i-ep-info-filled/>
            </el-icon>
            <p>请选择一个好友查看详情</p>
          </div>
        </div>
      </div>
    </el-main>

    <!-- AI 对话框 -->
    <el-dialog v-model="aiDialogVisible" title="AI 助手" width="600px">
      <div style="height: 400px; overflow-y: auto; margin-bottom: 20px;">
        <div v-for="(message, index) in aiMessages" :key="index" style="margin-bottom: 10px;">
          <el-tag v-if="message.role === 'user'" type="info" style="margin-right: 10px;">你</el-tag>
          <el-tag v-else type="success" style="margin-right: 10px;">AI</el-tag>
          <span>{{ message.content }}</span>
        </div>
      </div>
      <el-input
          v-model="aiInput"
          placeholder="输入你的问题"
          @keyup.enter="sendToAI"
      >
        <template #append>
          <el-button type="primary" @click="sendToAI">发送</el-button>
        </template>
      </el-input>
    </el-dialog>
  </div>
</template>

<script src="@/assets/styles/FriendPage/Friend.js"></script>
<style src="@/assets/styles/FriendPage/Friend.css"></style>