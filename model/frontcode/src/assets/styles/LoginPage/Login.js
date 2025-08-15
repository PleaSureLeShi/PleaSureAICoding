import axios from 'axios';
import {ref} from 'vue';
import {User, Lock, View, Hide, UserFilled, Iphone, Message} from '@element-plus/icons-vue';
import request from '@/utils/request'
import auth from "@/utils/auth.js";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";

const currentView = ref('welcome');
const showPassword = ref(false);
const showRegisterPassword = ref(false);
const showConfirmPassword = ref(false);
const showResetPassword = ref(false);
const showConfirmResetPassword = ref(false);

const loginFormRef = ref(null);
const registerFormRef = ref(null);
const resetFormRef = ref(null);

const loginForm = ref({
    account: '',
    password: '',
    remember: false
});

const registerForm = ref({
    username: '',
    nickname: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreement: false
});

const resetForm = ref({
    account: '',
    newPassword: '',
    confirmNewPassword: ''
});

const validatePass = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入密码'));
    } else {
        if (registerForm.value.confirmPassword !== '') {
            if (registerFormRef.value) {
                registerFormRef.value.validateField('confirmPassword');
            }
        }
        callback();
    }
};

const validateConfirmPass = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'));
    } else if (value !== registerForm.value.password) {
        callback(new Error('两次输入密码不一致'));
    } else {
        callback();
    }
};

const loginRules = {
    account: [{required: true, message: '请输入账号', trigger: 'blur'}],
    password: [{required: true, message: '请输入密码', trigger: 'blur'}]
};

const registerRules = {
    username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
    nickname: [{required: true, message: '请输入昵称', trigger: 'blur'}],
    phone: [
        {required: true, message: '请输入手机号', trigger: 'blur'},
        {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur'}
    ],
    email: [
        {required: true, message: '请输入邮箱', trigger: 'blur'},
        {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
    ],
    password: [{validator: validatePass, trigger: 'blur'}],
    confirmPassword: [{validator: validateConfirmPass, trigger: 'blur'}]
};

const resetRules = {
    account: [{required: true, message: '请输入手机号或邮箱', trigger: 'blur'}],
    newPassword: [{required: true, message: '请输入新密码', trigger: 'blur'}],
    confirmNewPassword: [{required: true, message: '请确认新密码', trigger: 'blur'}]
};

// 登录接口
// 统一使用封装的 request
const handleLogin = async () => {
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

// 注册接口
const handleRegister = async () => {
    if (!registerFormRef.value) return;
    if (!registerForm.value.agreement) {
        ElMessage.error('请先同意用户协议和隐私政策');
        return;
    }
    await registerFormRef.value.validate((valid) => {
        if (valid) {
            // 使用try-catch包装，避免任何错误输出到控制台
            const registerRequest = async () => {
                try {
                    const response = await axios.post('http://localhost:8080/api/register', registerForm.value);
                    if (response.data.success) {
                        currentView.value = 'login';
                        ElMessage.success('注册成功！正在跳转到登录页面.....');
                    } else {
                        // 根据错误类型显示中文提示
                        const errorMessage = response.data.message || '';
                        if (errorMessage.includes('username')) {
                            ElMessage.error('用户名已被使用，请选择其他用户名');
                        } else if (errorMessage.includes('phone')) {
                            ElMessage.error('手机号已被使用，请使用其他手机号');
                        } else if (errorMessage.includes('email')) {
                            ElMessage.error('邮箱已被使用，请使用其他邮箱');
                        } else {
                            ElMessage.error('注册失败，请检查输入信息');
                        }
                    }
                } catch (error) {
                    // 完全静默处理错误，不输出任何信息到控制台
                    if (error.response) {
                        const status = error.response.status;
                        if (status === 400) {
                            ElMessage.error('注册信息有误，请检查后重试');
                        } else if (status >= 500) {
                            ElMessage.error('服务器暂时无法处理请求，请稍后重试');
                        } else {
                            ElMessage.error('注册失败，请稍后重试');
                        }
                    } else {
                        ElMessage.error('网络连接异常，请检查网络后重试');
                    }
                    // 阻止错误传播
                    return;
                }
            };
            
            registerRequest();
        }
    });
};

// 重置密码接口
const handleResetPassword = async () => {
    try {
        if (!resetFormRef.value) return
        // 表单验证
        await resetFormRef.value.validate()
        // 使用封装的request
        const res = await request.post('/api/resetPassword', {
            account: resetForm.value.account,
            newPassword: resetForm.value.newPassword,
            confirmNewPassword: resetForm.value.confirmNewPassword
        })
        // 显示成功提示
        ElMessage.success({
            message: '密码重置成功，请使用新密码登录',
            duration: 3000
        })
        // 跳转登录页
        currentView.value = 'login'
        // 清空表单
        resetForm.value = {
            account: '',
            newPassword: '',
            confirmNewPassword: ''
        }
    } catch (error) {
        // 显示具体错误信息
        ElMessage.error({
            message: error.response?.data?.message || '密码重置失败，请检查输入信息',
            duration: 3000
        })
    }
}

export {
    currentView,
    showPassword,
    showRegisterPassword,
    showConfirmPassword,
    showResetPassword,
    showConfirmResetPassword,
    loginFormRef,
    registerFormRef,
    resetFormRef,
    loginForm,
    registerForm,
    resetForm,
    loginRules,
    registerRules,
    resetRules,
    handleLogin,
    handleRegister,
    handleResetPassword
};
