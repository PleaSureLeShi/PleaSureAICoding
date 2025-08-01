<template>
  <div id="userRegisterPage">
    <h2 class="title">PleaSure云图库 - 用户注册</h2>
    <div class="desc">智能协同云图库</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[{ required: true, message: '请输入密码' },{ min: 8, message: '密码不能小于 8 位' },]">
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[{ required: true, message: '请输入确认密码' },{ min: 8, message: '确认密码不能小于 8 位' },]">
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>
      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLoginUsingPost, userRegisterUsingPost } from '@/api/userController'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
//用于接收表单输入
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

const handleSubmit = async (values: any) => {
  const res = await userRegisterUsingPost(values)
  if(values.userPassword !== values.checkPassword){
    message.error('密码不一致');
    return;
  }
  // 注册成功，跳转到注册页面
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}


const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo)
}

</script>
<style scoped>
#userRegisterPage{
  max-width: 360px;
  margin: 0 auto;
}
.title{
  text-align: center;
  margin-bottom: 20px;
}
.desc{
  text-align: center;
  margin-bottom: 20px;
  color: #999;
}
.tips{
  text-align: right;
  margin-bottom: 16px;
  color: #999;

}
</style>
