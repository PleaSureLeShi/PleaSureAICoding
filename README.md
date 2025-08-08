# PleaSureAICoding

## Introduction/介绍
一款基于Vue3 + Ts + Antdesign + Axios + Pinia + SpringBoot + MyBatis + Mysql + LangChain4j的前后端分离项目，实现用户管理、代码流式输出、可视化编辑等功能。

## Back-end Configration/后端配置
### 环境配置
Mysql 8.x Or 5.x (推荐使用8.x)
Redis 7.2x
Nginx 1.2.7
可直接运行create_mysql.sql获取完全匹配的库表结构
配置成功后可直接在application主程序入口中启动

### 需要注意的点
记得修改pom.xml、application.yml
需要本地配置application-local.yml来调用大模型的api

## Front-end Configration/前端配置
Node.js >= 20.15.1 (推荐使用20.15.1 或 22.12.0)

可运行如下代码安装
```
nvm use 20.15.1
npm install
```
可运行如下代码运行
```
npm run dev
```
openapi前后端接口文件输出可直接在package.json中运行获取

本项目仅供学习使用，希望您能点击star
若有疏漏之处望您理解，谢谢！
