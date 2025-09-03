<template>
  <div id="app" style="display: flex; flex-direction: column; min-height: 100vh;">
    <el-header id="head">
      <div id="headContent" style="width: 100%; display: flex; align-items: center; justify-content: space-between;">
        <div style="display: flex; align-items: center;">
          <span style="font-size: 1.5em; font-weight: bold;">伙伴匹配平台</span>
          <div style="margin-left: 20px; display: flex;">
            <el-button @click="goToHomePage">首页</el-button>
            <el-button @click="goToFriendPage">好友</el-button>
            <el-button @click="goToRoomPage">房间</el-button>
            <el-button @click="goToMyPage">我的</el-button>
            <el-button v-if="user.role === 1" @click="goToMasterPage">平台管理</el-button>
            <el-button v-if="user.role === 2" @click="goToAdminPage">平台管理</el-button>
          </div>
        </div>
        <div style="display: flex; align-items: center;">
          <el-dropdown trigger="hover" @command="handleCommand">
            <span class="el-dropdown-link">
              <img :src="user.avatar" alt="User Avatar" style="width: 40px; height: 40px; border-radius: 50%;">
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <p style="text-align: center">昵称: {{ user.nickname }}</p>
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

    <el-main style="margin-top: 60px; flex-grow: 1;">
      <div style="display: flex; height: 100%;">
        <div style="width: 30%; background-color: #f5f7fa; padding: 20px; overflow-y: auto;">
          <h2>房间列表</h2>
          <div style="display: flex; gap: 10px;">
            <el-button type="primary" @click="showCreateRoomDialog">新建房间</el-button>
            <el-button type="success" @click="showAIDialog">AI 助手</el-button>
          </div>
          <el-divider/>

          <h3>所有房间</h3>
          <el-table
              :data="hotRooms"
              :row-class-name="getRowClassName"
              style="width: 100%;"
              row-key="id"
              @row-click="selectRoom">
            <el-table-column prop="room_name" label="房间名"/>
            <el-table-column prop="owner_nickname" label="房主" width="120"/>
            <el-table-column prop="room_type" label="类型"/>
            <el-table-column prop="room_status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.room_status)">
                  {{ formatRoomStatus(row.room_status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="人数上限">
              <template #default="{ row }">{{ row.max_members }}人</template>
            </el-table-column>
          </el-table>

          <h3 style="margin-top: 20px;">已加入房间</h3>
          <el-table
              :data="joinedRooms"
              :row-class-name="getRowClassName"
              style="width: 100%;"
              @row-click="selectRoom"
              row-key="id">
            <el-table-column prop="room_name" label="房间名"/>
            <el-table-column prop="owner_nickname" label="房主" width="120"/>
            <el-table-column prop="room_type" label="类型"/>
            <el-table-column prop="room_status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.room_status)">
                  {{ formatRoomStatus(row.room_status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="人数">
              <template #default="{ row }">{{ row.current_members }}/{{ row.max_members }}人</template>
            </el-table-column>
          </el-table>
        </div>

        <div style="width: 70%; padding: 20px;">
          <div v-if="selectedRoom">
            <div style="display: flex; align-items: center; gap: 10px;">
              <h2>{{ selectedRoom.room_name }}</h2>
              <el-tag>{{ selectedRoom.room_type }}</el-tag>
              <el-tag :type="getStatusTagType(selectedRoom.room_status)">
                {{ formatRoomStatus(selectedRoom.room_status) }}
              </el-tag>
              <span style="margin-left: auto;">
                <el-button
                    v-if="!isJoined"
                    type="primary"
                    @click="joinRoom">
                  加入房间
                </el-button>
                <el-button
                    v-else
                    type="danger"
                    @click="leaveRoom">
                  退出房间
                </el-button>
              </span>
            </div>

            <el-divider/>
            <h3>房间成员</h3>
            <template v-if="isJoined">
              <el-table :data="roomMembers" style="width: 100%">
                <el-table-column prop="nickname" label="昵称"/>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button
                        v-if="row.id !== user.id"
                        type="primary"
                        size="small"
                        @click="addFriend(row)">
                      加好友
                    </el-button>
                    <span v-else>自己</span>
                  </template>
                </el-table-column>
              </el-table>
            </template>
            <template v-else>
              <div class="empty-tip">
                <el-icon>
                  <i-ep-info-filled/>
                </el-icon>
                <span>需要加入房间后才能查看成员信息</span>
              </div>
            </template>
            <h3 style="margin-top: 20px;">房间聊天</h3>
            <template v-if="isJoined">
              <div class="chat-container">
                <div style="border: 1px solid #ddd; padding: 10px; height: 300px; overflow-y: auto;">
                  <div v-for="message in roomMessages" :key="message.id" style="margin-bottom: 10px;">
                    <template v-if="message.status === 'deleted'">
                      <span style="color: #999; font-style: italic;">
                        {{ message.sender_name }} 撤回了一条消息
                      </span>
                    </template>
                    <template v-else>
                      <strong>{{ message.sender_name }}:</strong>
                      <template v-if="message.type === 'FILE'">
                        <span style="display: inline-block;">
                          <el-link
                              type="primary"
                              :underline="false"
                              @click="openFileInNewWindow(message.content)">
                            {{ message.fileName || '未命名文件' }}
                          </el-link>
                          <span style="color: #999; margin-left: 5px;">
                          </span>
                        </span>
                      </template>
                      <template v-else>
                        {{ message.content }}
                      </template>
                    </template>
                    <el-button
                        v-if="message.senderId === user.id && message.status !== 'deleted'"
                        type="text"
                        @click.stop="recallMessage(message)"
                        style="margin-left: 10px;">
                      撤回
                    </el-button>
                    <span style="color: #999; margin-left: 10px; font-size: 0.8em;">
                      {{ message.timestamp }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="empty-tip">
                <el-icon>
                  <i-ep-info-filled/>
                </el-icon>
                <span>需要加入房间后才能查看聊天记录</span>
              </div>
            </template>
            <el-input
                v-model="newMessage"
                :disabled="!isJoined"
                placeholder="输入消息"
                @keyup.enter="sendMessage">
            </el-input>
          </div>

          <div v-else style="text-align: center; margin-top: 100px;">
            <el-icon :size="50">
              <i-ep-info-filled/>
            </el-icon>
            <p>请选择一个房间查看详情</p>
          </div>
        </div>
      </div>
    </el-main>

    <el-dialog v-model="createRoomDialogVisible" title="新建房间">
      <el-form :model="newRoomForm" label-width="100px">
        <el-form-item label="房间名" required>
          <el-input v-model="newRoomForm.room_name"/>
        </el-form-item>
        <el-form-item label="房间描述" required>
          <el-input v-model="newRoomForm.room_desc" type="textarea"/>
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="newRoomForm.room_type" placeholder="请选择">
            <el-option label="生活" value="game"/>
            <el-option label="学习" value="study"/>
            <el-option label="社交" value="social"/>
          </el-select>
        </el-form-item>
        <el-form-item label="房间状态">
          <el-select v-model="newRoomForm.room_status" placeholder="请选择">
            <el-option label="公开" value="public"/>
            <el-option label="私密" value="private"/>
            <!--            <el-option label="加密" value="encrypted"/>-->
          </el-select>
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="newRoomForm.max_members" :min="1" :max="100"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createRoom">创建</el-button>
      </template>
    </el-dialog>

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
<script src="@/assets/styles/RoomPage/Room.js"></script>
<style scoped src="@/assets/styles/RoomPage/Room.css"></style>
