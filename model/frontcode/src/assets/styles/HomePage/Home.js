import image1 from '@/assets/images/pic_1.png';
import image2 from '@/assets/images/pic_2.png';
import image3 from '@/assets/images/pic_3.png';
import logo from '@/assets/images/SystemLogo.png';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import auth from "@/utils/auth.js";
import response from "assert";

export default {
    data() {
        return {
            // 轮播图
            images: [
                image1,
                image2,
                image3
            ],
            // 平台介绍
            introduceItems: [
                { title: '智能匹配', description: '基于你的兴趣和标签，我们为你推荐最合适的伙伴和房间' },
                { title: '多样化房间', description: '从学习到生活，各种类型的房间满足你的不同需求' },
                { title: '实时互动', description: '与新朋友即时聊天，分享你的想法和经验' }
            ],
            // 引用名言
            quotes: [
                { quote: "只有不断前行，才能遇见更好的自己。", author: "阿尔伯特·爱因斯坦" },
                { quote: "不要等待机会，而要创造机会。", author: "乔治·伯纳德·肖" },
                { quote: "真正的友谊不是基于利益，而是建立在相互理解和支持之上。", author: "奥斯卡·王尔德" },
                { quote: "每天进步一点点。", author: "雷锋" },
                { quote: "一个人的价值，在于他能给周围的人带来多少正能量。", author: "阿尔伯特·爱因斯坦" },
                { quote: "成功的秘诀在于坚持不懈奋斗。", author: "伏尔泰" },
                { quote: "我们都是旅人，我们的路偶然交叉。这交会能撞出什么火花，就看我们怎么对待彼此。", author: "安东尼·德·圣-埃克苏佩里" },
                { quote: "失败是成功的垫脚石。", author: "陈独秀" },
                { quote: "坚持不懈，方有所成。", author: "宋庆龄" },
                { quote: "人生中最大的幸福在于为他人而活。", author: "阿尔贝·史怀哲" }
            ],
            // 用户信息
            user: {
                id: 2, // 用户ID
                username: '', // 用户名
                nickname: '', // 昵称
                avatar: logo, // 用户头像
                friendCount: 0, // 好友数量
                roomCount: 0, // 加入房间数量
                role: 3, // 用户角色（1: 站长, 2: 管理员, 3: 普通用户）
                status: 1, // 用户状态（1: 正常, 0: 封禁）
                isOnline: 0, // 在线状态（1: 在线, 0: 离线）
            },
            loading: false, // 加载状态
        };
    },
    computed: {
        randomQuote() {
            const randomIndex = Math.floor(Math.random() * this.quotes.length);
            return this.quotes[randomIndex];
        }
    },
    methods: {
        // 处理下拉菜单命令
        handleCommand(command) {
            if (command === 'logout') {
                this.handleLogout();
            } else if (command === 'profile') {
                this.goToProfile();
            }
        },
        // 退出登录
        handleLogout() {
            localStorage.removeItem('access_token');
            localStorage.removeItem('user_info'); // 清除用户信息
            this.$router.push('/login'); // 跳转到登录页
        },
        // 进入个人中心
        goToProfile() {
            this.$router.push('/my');
        },
        // 跳转到好友页面
        goToFriendPage() {
            this.$router.push('/friend');
        },
        // 跳转到房间页面
        goToRoomPage() {
            this.$router.push('/room');
        },
        // 跳转到个人页面
        goToMyPage() {
            this.$router.push('/my');
        },
        // 跳转到首页
        goToHomePage() {
            this.$router.push('/');
        },
        // 跳转到站长管理页
        goToAdminPage() {
            this.$router.push('/admin');
        },
        // 跳转到管理员管理页
        goToMasterPage() {
            this.$router.push('/master');
        },
        // 获取用户信息
        async fetchUserInfo() {
            this.loading = true;
            try {
                // 从 localStorage 或 auth 模块获取用户ID
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户ID不存在");
                const res = await request.get(`/api/user/${userId}/info`);
                // 更新用户信息
                this.user = {
                    ...res.data,
                    id: res.data.id,
                    role: res.data.role_id || res.data.role,
                    avatar: res.data.avatar || logo
                };
            } catch (error) {
                this.$message.error('获取用户信息失败:', error);
            } finally {
                this.loading = false;
            }
        },

    },
    mounted() {
        // 获取用户信息
        this.fetchUserInfo();
    }
};
