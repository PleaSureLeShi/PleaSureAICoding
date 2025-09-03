-- 创建库
create database if not exists pleasure_aicoding;

-- 切换库
use pleasure_aicoding;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 修改用户头像字段类型为 LONGTEXT 以支持 base64 图片数据（如果需要设置头像的话就使用，不需要的话就不用运行）
ALTER TABLE `user`
    MODIFY COLUMN `userAvatar` LONGTEXT COMMENT '用户头像';

-- 应用表
create table if not exists app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table if not exists chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 房间表
CREATE TABLE IF NOT EXISTS `chat_room`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房间ID',
    `roomName`        VARCHAR(100) NOT NULL COMMENT '房间名称',
    `roomDescription` VARCHAR(500) NULL COMMENT '房间描述',
    `roomType`        VARCHAR(50)  NOT NULL COMMENT '房间类型：DataStructure-数据结构，ComputerOrganization-计算机组成原理，OperatingSystem-操作系统，ComputerNetwork-计算机网络，Integrated-综合',
    `maxMembers`      INT      DEFAULT 100 COMMENT '最大成员数',
    `currentMembers`  INT      DEFAULT 0 COMMENT '当前成员数',
    `ownerId`         BIGINT       NOT NULL COMMENT '房主ID',
    `isPublic`        TINYINT  DEFAULT 1 COMMENT '是否公开：0-私有，1-公开',
    `password`        VARCHAR(100) NULL COMMENT '房间密码（私有房间）',
    `status`          TINYINT  DEFAULT 1 COMMENT '房间状态：0-封禁，1-正常',
    `createTime`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`        TINYINT  DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_owner` (`ownerId`),
    INDEX `idx_type` (`roomType`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`ownerId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) COMMENT '聊天房间表';

-- 房间成员表
CREATE TABLE IF NOT EXISTS `room_member`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员关系ID',
    `roomId`       BIGINT   NOT NULL COMMENT '房间ID',
    `userId`       BIGINT   NOT NULL COMMENT '用户ID',
    `role`         VARCHAR(20) DEFAULT 'member' COMMENT '角色：owner-房主，admin-管理员，member-普通成员',
    `joinTime`     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `lastReadTime` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    `isMuted`      TINYINT     DEFAULT 0 COMMENT '是否被禁言：0-否，1-是',
    `mutedUntil`   DATETIME NULL COMMENT '禁言到期时间',
    `isDelete`     TINYINT     DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY `uk_room_user` (`roomId`, `userId`),
    INDEX `idx_user` (`userId`),
    INDEX `idx_room` (`roomId`),
    FOREIGN KEY (`roomId`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) COMMENT '房间成员表';

-- 好友关系表
CREATE TABLE IF NOT EXISTS `friendship`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '好友关系ID',
    `userId`         BIGINT       NOT NULL COMMENT '用户ID',
    `friendId`       BIGINT       NOT NULL COMMENT '好友ID',
    `status`         TINYINT  DEFAULT 0 COMMENT '关系状态：0-待确认，1-已同意，2-已拒绝，3-已拉黑',
    `requestMessage` VARCHAR(200) NULL COMMENT '好友申请消息',
    `requestTime`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `responseTime`   DATETIME     NULL COMMENT '响应时间',
    `remark`         VARCHAR(50)  NULL COMMENT '好友备注',
    `createTime`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`       TINYINT  DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY `uk_user_friend` (`userId`, `friendId`),
    INDEX `idx_friend` (`friendId`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`friendId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) COMMENT '好友关系表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `messageType`  VARCHAR(20)  NOT NULL COMMENT '消息类型：room-群聊，private-私聊',
    `roomId`       BIGINT       NULL COMMENT '房间ID（群聊消息）',
    `senderId`     BIGINT       NOT NULL COMMENT '发送者ID',
    `receiverId`   BIGINT       NULL COMMENT '接收者ID（私聊消息）',
    `contentType`  VARCHAR(20) DEFAULT 'text' COMMENT '内容类型：text-文本，image-图片，system-系统消息',
    `content`      TEXT         NOT NULL COMMENT '消息内容',
    `fileUrl`      VARCHAR(500) NULL COMMENT '文件URL（图片消息）',
    `fileName`     VARCHAR(200) NULL COMMENT '文件名',
    `fileSize`     BIGINT       NULL COMMENT '文件大小（字节）',
    `replyToId`    BIGINT       NULL COMMENT '回复的消息ID',
    `isRecalled`   TINYINT     DEFAULT 0 COMMENT '是否已撤回：0-否，1-是',
    `sendTime`     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `isDelete`     TINYINT     DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_room` (`roomId`),
    INDEX `idx_sender` (`senderId`),
    INDEX `idx_receiver` (`receiverId`),
    INDEX `idx_send_time` (`sendTime`),
    INDEX `idx_message_type` (`messageType`),
    INDEX `idx_reply` (`replyToId`),
    FOREIGN KEY (`roomId`) REFERENCES `chat_room` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`senderId`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiverId`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`replyToId`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL
) COMMENT '聊天消息表';

-- 消息阅读状态表
CREATE TABLE IF NOT EXISTS `message_read_status`
(
    `id`        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '阅读状态ID',
    `messageId` BIGINT NOT NULL COMMENT '消息ID',
    `userId`    BIGINT NOT NULL COMMENT '用户ID',
    `readTime`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY `uk_message_user` (`messageId`, `userId`),
    INDEX `idx_user` (`userId`),
    FOREIGN KEY (`messageId`) REFERENCES `chat_message` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) COMMENT '消息阅读状态表';

-- 用户会话表（记录用户的聊天会话）
CREATE TABLE IF NOT EXISTS `user_conversation`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    `userId`           BIGINT      NOT NULL COMMENT '用户ID',
    `conversationType` VARCHAR(20) NOT NULL COMMENT '会话类型：room-群聊，private-私聊',
    `targetId`         BIGINT      NOT NULL COMMENT '目标ID（房间ID或好友ID）',
    `lastMessageId`    BIGINT      NULL COMMENT '最后一条消息ID',
    `lastMessageTime`  DATETIME    NULL COMMENT '最后消息时间',
    `unreadCount`      INT      DEFAULT 0 COMMENT '未读消息数',
    `isTop`            TINYINT  DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `isMuted`          TINYINT  DEFAULT 0 COMMENT '是否免打扰：0-否，1-是',
    `isHidden`         TINYINT  DEFAULT 0 COMMENT '是否隐藏：0-否，1-是',
    `createTime`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_conversation` (`userId`, `conversationType`, `targetId`),
    INDEX `idx_user` (`userId`),
    INDEX `idx_target` (`targetId`),
    INDEX `idx_last_message_time` (`lastMessageTime`),
    FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`lastMessageId`) REFERENCES `chat_message` (`id`) ON DELETE SET NULL
) COMMENT '用户会话表';

-- 系统通知表
CREATE TABLE IF NOT EXISTS `system_notification`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    `userId`     BIGINT       NOT NULL COMMENT '接收用户ID',
    `type`       VARCHAR(50)  NOT NULL COMMENT '通知类型：friend_request-好友申请，room_invite-房间邀请，system-系统通知',
    `title`      VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content`    VARCHAR(500) NOT NULL COMMENT '通知内容',
    `relatedId`  BIGINT       NULL COMMENT '关联ID（好友申请ID、房间ID等）',
    `isRead`     TINYINT  DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `readTime`   DATETIME     NULL COMMENT '阅读时间',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user` (`userId`),
    INDEX `idx_type` (`type`),
    INDEX `idx_is_read` (`isRead`),
    FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) COMMENT '系统通知表';