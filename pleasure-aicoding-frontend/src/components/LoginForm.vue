<template>
  <div class="login-container">
    <h2>欢迎使用在线聊天</h2>
    <el-form :model="loginForm" :rules="rules" ref="loginForm">
      <el-form-item prop="nickname">
        <el-input 
          v-model="loginForm.nickname" 
          placeholder="请输入英文昵称"
          prefix-icon="el-icon-user"
        >
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitLogin" :loading="loading">
          登录聊天室
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'LoginForm',
  data() {
    // 验证昵称是否为英文
    const validateNickname = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请输入昵称'))
      }
      if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        return callback(new Error('昵称只能包含英文字母、数字和下划线'))
      }
      callback()
    }
    
    return {
      loginForm: {
        nickname: ''
      },
      rules: {
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { validator: validateNickname, trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    submitLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          // 模拟登录过程
          setTimeout(() => {
            this.loading = false
            this.$emit('login', { 
              id: this.loginForm.nickname,
              nickname: this.loginForm.nickname,
              loginTime: new Date().toISOString()
            })
          }, 500)
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

h2 {
  margin-bottom: 30px;
  color: #409EFF;
}
</style>