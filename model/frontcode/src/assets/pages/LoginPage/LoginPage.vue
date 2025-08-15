<template>
  <div class="app-container">
    <div class="main-container">
      <!-- 主内容区 -->
      <div class="content-box">
        <!-- Logo区域 -->
        <div class="logo-section">
          <h1 class="main-title">智友同行 慧创未来</h1>
          <p class="sub-title">智能伙伴平台</p>
        </div>
        <!-- 欢迎页 -->
        <div v-if="currentView === 'welcome'" class="welcome-section">
          <div class="welcome-header">
            <h2 class="welcome-title">Welcome to join us</h2>
            <p class="welcome-subtitle">找到最适合您的合作伙伴</p>
          </div>
          <div class="button-group">
            <el-button type="primary" size="large" class="flex-button !rounded-button" @click="currentView = 'login'">
              立即登录
            </el-button>
            <el-button size="large" class="flex-button !rounded-button" @click="currentView = 'register'">
              快速注册
            </el-button>
          </div>
        </div>

        <!-- 登录表单 -->
        <el-form v-if="currentView === 'login'" :model="loginForm" :rules="loginRules" ref="loginFormRef"
                 class="form-container">
          <el-form-item prop="account">
            <el-input v-model="loginForm.account" placeholder="请输入用户名/手机号/邮箱" size="large">
              <template #prefix>
                <el-icon>
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码"
                      size="large">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showPassword = !showPassword">
                  <View v-if="showPassword"/>
                  <Hide v-else/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <div class="form-footer">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <el-button link @click="currentView = 'resetPassword'">忘记密码？</el-button>
          </div>
          <el-button type="primary" size="large" class="submit-button !rounded-button" @click="handleLogin">
            登录
          </el-button>
          <div class="switch-form">
            <span class="text-prompt">还没有账号？</span>
            <el-button link @click="currentView = 'register'">立即注册</el-button>
          </div>
        </el-form>

        <!-- 注册表单 -->
        <el-form v-if="currentView === 'register'" :model="registerForm" :rules="registerRules" ref="registerFormRef"
                 class="form-container">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large">
              <template #prefix>
                <el-icon>
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="registerForm.nickname" placeholder="请输入昵称" size="large">
              <template #prefix>
                <el-icon>
                  <UserFilled/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" size="large">
              <template #prefix>
                <el-icon>
                  <Iphone/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" size="large">
              <template #prefix>
                <el-icon>
                  <Message/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" :type="showRegisterPassword ? 'text' : 'password'"
                      placeholder="请输入密码" size="large">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showRegisterPassword = !showRegisterPassword">
                  <View v-if="showRegisterPassword"/>
                  <Hide v-else/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"
                      placeholder="请确认密码" size="large">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showConfirmPassword = !showConfirmPassword">
                  <View v-if="showConfirmPassword"/>
                  <Hide v-else/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <div class="agreement-section">
            <el-checkbox v-model="registerForm.agreement"></el-checkbox>
            <span class="text-prompt">我已阅读并同意</span>
            <el-link type="primary" @click="showAgreement">《用户协议》</el-link>
          </div>
          <el-button type="primary" size="large" class="submit-button !rounded-button" @click="handleRegister">
            注册
          </el-button>
          <div class="switch-form">
            <span class="text-prompt">已有账号？</span>
            <el-button link @click="currentView = 'login'">立即登录</el-button>
          </div>
        </el-form>

        <!-- 重置密码表单 -->
        <el-form v-if="currentView === 'resetPassword'" :model="resetForm" :rules="resetRules" ref="resetFormRef"
                 class="form-container">
          <div class="reset-header">
            <h2 class="reset-title">重置密码</h2>
          </div>
          <el-form-item prop="account">
            <el-input v-model="resetForm.account" placeholder="请输入手机号或邮箱" size="large">
              <template #prefix>
                <el-icon>
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="newPassword">
            <el-input v-model="resetForm.newPassword" :type="showResetPassword ? 'text' : 'password'"
                      placeholder="请输入新密码" size="large">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showResetPassword = !showResetPassword">
                  <View v-if="showResetPassword"/>
                  <Hide v-else/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmNewPassword">
            <el-input v-model="resetForm.confirmNewPassword" :type="showConfirmResetPassword ? 'text' : 'password'"
                      placeholder="请确认新密码" size="large">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
              <template #suffix>
                <el-icon class="cursor-pointer" @click="showConfirmResetPassword = !showConfirmResetPassword">
                  <View v-if="showConfirmResetPassword"/>
                  <Hide v-else/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-button type="primary" size="large" class="submit-button !rounded-button" @click="handleResetPassword">
            重置密码
          </el-button>
          <div class="switch-form">
            <el-button link @click="currentView = 'login'">返回登录</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import * as loginModule from '@/assets/styles/LoginPage/Login.js';
import {Hide, Iphone, Lock, Message, User, UserFilled, View} from "@element-plus/icons-vue";
import {handleLogin, registerRules as response} from "@/assets/styles/LoginPage/Login.js";
import request from '@/utils/request'
import axios from 'axios'
import auth from "@/utils/auth.js";
import {ElMessage} from "element-plus";

export default {
  components: {Iphone, User, UserFilled, Message, Hide, View, Lock},
  setup() {
    return {
      ...loginModule
    };
  },
  methods: {
    showAgreement() {
      this.$alert(
          '1. 您不得泄漏他人隐私信息。\n\n' +
          '2. 您不得发布违法或不良信息。\n\n' +
          '3. 平台有权根据情况调整服务内容。\n\n' +
          '4. 若发现违反平台有权封禁您的账号。\n\n',
          '用户协议',
          {
            confirmButtonText: '我知道了',
            callback: () => {
              // 用户点击确认后的回调
              //  this.$message.log('用户已阅读协议');
            }
          }
      );
    },
    async handleLogin() {
      try {
        const res = await request.post('/api/login', {
          account: this.loginForm.account,
          password: this.loginForm.password,
          remember: this.loginForm.remember
        });
        const userInfoRes = await request.get(`/api/user/home/${res.data.id}/info`);
        if (userInfoRes.data.status === 0) { // 0表示封禁
          ElMessage.error('账号已被封禁');
          auth.clearAll();
          return;
        }
        if (res.data && res.data.id) {
          // 登录成功后，获取用户ID并存储
          localStorage.setItem('userId', res.data.id);
          auth.setToken(res.data.token);
          auth.setUserInfo({
            id: res.data.id,
            role: res.data.role,
            username: res.data.username,
            nickname: res.data.nickname,
            avatar: res.data.avatar,
            status: userInfoRes.data.status
          });
          // 跳转到首页
          this.$router.push(this.$route.query.redirect || '/home');
        } else {
          this.$message.error('登录失败: 未获取到用户信息');
        }
      } catch (error) {
        this.$message.error('登录失败: 请检查账号密码是否正确')
      }
    }
  }
};
</script>
<style scoped>
@import '@/assets/styles/LoginPage/Login.css';
</style>
