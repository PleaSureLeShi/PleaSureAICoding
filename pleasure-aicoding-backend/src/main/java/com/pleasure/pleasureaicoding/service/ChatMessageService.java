package com.pleasure.pleasureaicoding.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageQueryRequest;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageSendRequest;
import com.pleasure.pleasureaicoding.model.entity.ChatMessage;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatMessageVO;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     *
     * @param chatMessageSendRequest 发送请求
     * @param loginUser 登录用户
     * @return 消息ID
     */
    Long sendMessage(ChatMessageSendRequest chatMessageSendRequest, User loginUser);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param loginUser 登录用户
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, User loginUser);

    /**
     * 分页获取房间消息
     *
     * @param chatMessageQueryRequest 查询请求
     * @param loginUser 登录用户
     * @return 消息列表
     */
    Page<ChatMessageVO> listRoomMessageVOByPage(ChatMessageQueryRequest chatMessageQueryRequest, User loginUser);

    /**
     * 分页获取私聊消息
     *
     * @param chatMessageQueryRequest 查询请求
     * @param loginUser 登录用户
     * @return 消息列表
     */
    Page<ChatMessageVO> listPrivateMessageVOByPage(ChatMessageQueryRequest chatMessageQueryRequest, User loginUser);

    /**
     * 获取消息详情
     *
     * @param messageId 消息ID
     * @param loginUser 登录用户
     * @return 消息详情
     */
    ChatMessageVO getChatMessageVO(Long messageId, User loginUser);
}