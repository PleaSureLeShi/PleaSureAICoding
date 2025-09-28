package com.pleasure.pleasureaicoding.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.exception.ThrowUtils;
import com.pleasure.pleasureaicoding.mapper.ChatMessageMapper;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageQueryRequest;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageSendRequest;
import com.pleasure.pleasureaicoding.model.entity.ChatMessage;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatMessageVO;
import com.pleasure.pleasureaicoding.service.ChatMessageService;
import com.pleasure.pleasureaicoding.service.RoomMemberService;
import com.pleasure.pleasureaicoding.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pleasure.pleasureaicoding.model.entity.table.ChatMessageTableDef.CHAT_MESSAGE;
import static com.pleasure.pleasureaicoding.model.entity.table.UserTableDef.USER;

/**
 * 聊天消息服务实现类
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Resource
    private RoomMemberService roomMemberService;

    @Resource
    private UserService userService;

    @Override
    @Transactional
    public Long sendMessage(ChatMessageSendRequest chatMessageSendRequest, User loginUser) {
        ThrowUtils.throwIf(chatMessageSendRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        String messageType = chatMessageSendRequest.getMessageType();
        Long roomId = chatMessageSendRequest.getRoomId();
        Long receiverId = chatMessageSendRequest.getReceiverId();
        String content = chatMessageSendRequest.getContent();
        String contentType = chatMessageSendRequest.getContentType();
        Long replyToId = chatMessageSendRequest.getReplyToId();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        
        if ("room".equals(messageType)) {
            // 群聊消息
            ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
            
            // 检查用户是否在房间中
            boolean isInRoom = roomMemberService.isUserInRoom(roomId, loginUser.getId());
            ThrowUtils.throwIf(!isInRoom, ErrorCode.FORBIDDEN_ERROR, "您不在该房间中");
            
        } else if ("private".equals(messageType)) {
            // 私聊消息
            ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR, "接收者ID不能为空");
            ThrowUtils.throwIf(receiverId.equals(loginUser.getId()), ErrorCode.PARAMS_ERROR, "不能给自己发消息");
            
            // 检查接收者是否存在
            User receiver = userService.getById(receiverId);
            ThrowUtils.throwIf(receiver == null, ErrorCode.NOT_FOUND_ERROR, "接收者不存在");
            
        } else {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        }

        // 如果是回复消息，检查被回复的消息是否存在
        if (replyToId != null && replyToId > 0) {
            ChatMessage replyMessage = this.getById(replyToId);
            ThrowUtils.throwIf(replyMessage == null, ErrorCode.NOT_FOUND_ERROR, "被回复的消息不存在");
        }

        // 创建消息
        ChatMessage chatMessage = ChatMessage.builder()
                .messageType(messageType)
                .roomId(roomId)
                .senderId(loginUser.getId())
                .receiverId(receiverId)
                .contentType(StrUtil.isBlank(contentType) ? "text" : contentType)
                .content(content)
                .replyToId(replyToId)
                .isRecalled(0)
                .sendTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        boolean saveResult = this.save(chatMessage);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "发送消息失败");

        return chatMessage.getId();
    }

    @Override
    @Transactional
    public boolean recallMessage(Long messageId, User loginUser) {
        ThrowUtils.throwIf(messageId == null || messageId <= 0, ErrorCode.PARAMS_ERROR, "消息ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 获取消息
        ChatMessage chatMessage = this.getById(messageId);
        ThrowUtils.throwIf(chatMessage == null, ErrorCode.NOT_FOUND_ERROR, "消息不存在");
        ThrowUtils.throwIf(chatMessage.getIsRecalled() == 1, ErrorCode.PARAMS_ERROR, "消息已被撤回");
        
        // 只能撤回自己的消息
        ThrowUtils.throwIf(!chatMessage.getSenderId().equals(loginUser.getId()), 
                ErrorCode.FORBIDDEN_ERROR, "只能撤回自己的消息");
        
        // 检查撤回时间限制（2分钟内）
        LocalDateTime sendTime = chatMessage.getSendTime();
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(sendTime, now).toMinutes();
        ThrowUtils.throwIf(minutes > 2, ErrorCode.FORBIDDEN_ERROR, "消息发送超过2分钟，无法撤回");

        // 撤回消息
        ChatMessage updateMessage = ChatMessage.builder()
                .id(messageId)
                .isRecalled(1)
                .isDelete(1)
                .build();

        return this.updateById(updateMessage);
    }

    @Override
    public Page<ChatMessageVO> listRoomMessageVOByPage(ChatMessageQueryRequest chatMessageQueryRequest, User loginUser) {
        ThrowUtils.throwIf(chatMessageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long roomId = chatMessageQueryRequest.getRoomId();
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        
        // 检查用户是否在房间中
        boolean isInRoom = roomMemberService.isUserInRoom(roomId, loginUser.getId());
        ThrowUtils.throwIf(!isInRoom, ErrorCode.FORBIDDEN_ERROR, "您不在该房间中");
        
        long pageNum = chatMessageQueryRequest.getPageNum();
        long pageSize = chatMessageQueryRequest.getPageSize();
        LocalDateTime lastMessageTime = chatMessageQueryRequest.getLastMessageTime();
        
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(CHAT_MESSAGE.ALL_COLUMNS, USER.USER_NAME.as("senderName"), USER.USER_AVATAR.as("senderAvatar"))
                .from(CHAT_MESSAGE)
                .leftJoin(USER).on(CHAT_MESSAGE.SENDER_ID.eq(USER.ID))
                .where(CHAT_MESSAGE.MESSAGE_TYPE.eq("room"))
                .and(CHAT_MESSAGE.ROOM_ID.eq(roomId))
                .and(CHAT_MESSAGE.IS_DELETE.eq(0))
                .orderBy(CHAT_MESSAGE.SEND_TIME, false);
        
        // 游标分页
        if (lastMessageTime != null) {
            queryWrapper.and(CHAT_MESSAGE.SEND_TIME.lt(lastMessageTime));
        }
        
        // 分页查询
        Page<ChatMessageVO> chatMessagePage = this.pageAs(Page.of(pageNum, pageSize), queryWrapper, ChatMessageVO.class);
        return chatMessagePage;
    }

    @Override
    public Page<ChatMessageVO> listPrivateMessageVOByPage(ChatMessageQueryRequest chatMessageQueryRequest, User loginUser) {
        ThrowUtils.throwIf(chatMessageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long receiverId = chatMessageQueryRequest.getReceiverId();
        ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR, "接收者ID不能为空");
        
        long pageNum = chatMessageQueryRequest.getPageNum();
        long pageSize = chatMessageQueryRequest.getPageSize();
        LocalDateTime lastMessageTime = chatMessageQueryRequest.getLastMessageTime();
        
        // 构建查询条件（查询双方的消息）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(CHAT_MESSAGE.ALL_COLUMNS, USER.USER_NAME.as("senderName"), USER.USER_AVATAR.as("senderAvatar"))
                .from(CHAT_MESSAGE)
                .leftJoin(USER).on(CHAT_MESSAGE.SENDER_ID.eq(USER.ID))
                .where(CHAT_MESSAGE.MESSAGE_TYPE.eq("private"))
                .and(CHAT_MESSAGE.IS_DELETE.eq(0))
                .and(
                    CHAT_MESSAGE.SENDER_ID.eq(loginUser.getId()).and(CHAT_MESSAGE.RECEIVER_ID.eq(receiverId))
                    .or(CHAT_MESSAGE.SENDER_ID.eq(receiverId).and(CHAT_MESSAGE.RECEIVER_ID.eq(loginUser.getId())))
                )
                .orderBy(CHAT_MESSAGE.SEND_TIME, false);
        
        // 游标分页
        if (lastMessageTime != null) {
            queryWrapper.and(CHAT_MESSAGE.SEND_TIME.lt(lastMessageTime));
        }
        
        // 分页查询
        Page<ChatMessageVO> chatMessagePage = this.pageAs(Page.of(pageNum, pageSize), queryWrapper, ChatMessageVO.class);
        
        return chatMessagePage;
    }

    @Override
    public ChatMessageVO getChatMessageVO(Long messageId, User loginUser) {
        ThrowUtils.throwIf(messageId == null || messageId <= 0, ErrorCode.PARAMS_ERROR, "消息ID不能为空");
        
        // 查询消息
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(CHAT_MESSAGE.ALL_COLUMNS, USER.USER_NAME.as("senderName"), USER.USER_AVATAR.as("senderAvatar"))
                .from(CHAT_MESSAGE)
                .leftJoin(USER).on(CHAT_MESSAGE.SENDER_ID.eq(USER.ID))
                .where(CHAT_MESSAGE.ID.eq(messageId))
                .and(CHAT_MESSAGE.IS_DELETE.eq(0));

        ChatMessageVO chatMessage = this.getOneAs(queryWrapper, ChatMessageVO.class);
        ThrowUtils.throwIf(chatMessage == null, ErrorCode.NOT_FOUND_ERROR, "消息不存在");
        
        // 权限检查
        if ("room".equals(chatMessage.getMessageType())) {
            // 群聊消息：检查用户是否在房间中
            if (loginUser != null) {
                boolean isInRoom = roomMemberService.isUserInRoom(chatMessage.getRoomId(), loginUser.getId());
                ThrowUtils.throwIf(!isInRoom, ErrorCode.FORBIDDEN_ERROR, "您不在该房间中");
            }
        } else if ("private".equals(chatMessage.getMessageType())) {
            // 私聊消息：检查用户是否为发送者或接收者
            if (loginUser != null) {
                boolean isParticipant = chatMessage.getSenderId().equals(loginUser.getId()) 
                        || chatMessage.getReceiverId().equals(loginUser.getId());
                ThrowUtils.throwIf(!isParticipant, ErrorCode.FORBIDDEN_ERROR, "您无权查看该消息");
            }
        }
        
        return chatMessage;
    }

    /**
     * 转换为ChatMessageVO分页对象
     */
    private Page<ChatMessageVO> getChatMessageVOPage(Page<ChatMessage> chatMessagePage) {
        List<ChatMessage> chatMessageList = chatMessagePage.getRecords();
        if (chatMessageList.isEmpty()) {
            return new Page<>(chatMessagePage.getPageNumber(), chatMessagePage.getPageSize(), chatMessagePage.getTotalRow());
        }
        
        // 转换为VO
        List<ChatMessageVO> chatMessageVOList = chatMessageList.stream().map(chatMessage -> {
            ChatMessageVO chatMessageVO = new ChatMessageVO();
            BeanUtils.copyProperties(chatMessage, chatMessageVO);
            return chatMessageVO;
        }).collect(Collectors.toList());
        
        Page<ChatMessageVO> chatMessageVOPage = new Page<>(chatMessagePage.getPageNumber(), 
                chatMessagePage.getPageSize(), chatMessagePage.getTotalRow());
        chatMessageVOPage.setRecords(chatMessageVOList);
        return chatMessageVOPage;
    }
}