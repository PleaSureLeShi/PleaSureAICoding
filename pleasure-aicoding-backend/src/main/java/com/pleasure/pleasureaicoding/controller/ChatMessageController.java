package com.pleasure.pleasureaicoding.controller;

import com.mybatisflex.core.paginate.Page;
import com.pleasure.pleasureaicoding.annotation.AuthCheck;
import com.pleasure.pleasureaicoding.common.BaseResponse;
import com.pleasure.pleasureaicoding.common.ResultUtils;
import com.pleasure.pleasureaicoding.constant.UserConstant;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.exception.ThrowUtils;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageQueryRequest;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageSendRequest;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatMessageVO;
import com.pleasure.pleasureaicoding.service.ChatMessageService;
import com.pleasure.pleasureaicoding.service.UserService;
import com.pleasure.pleasureaicoding.websocket.ChatRoomWebSocketServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天消息接口
 *
 * @author PleaSure乐事
 */
@RestController
@RequestMapping("/chatMessage")
@Slf4j
public class ChatMessageController {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private UserService userService;

    /**
     * 发送消息（HTTP接口，主要用于文件上传等特殊消息）
     *
     * @param chatMessageSendRequest 发送请求
     * @param request HTTP请求
     * @return 消息ID
     */
    @PostMapping("/send")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Long> sendMessage(@RequestBody ChatMessageSendRequest chatMessageSendRequest,
                                        HttpServletRequest request) {
        ThrowUtils.throwIf(chatMessageSendRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        Long messageId = chatMessageService.sendMessage(chatMessageSendRequest, loginUser);
        
        // 如果是房间消息，通过WebSocket广播
        if ("room".equals(chatMessageSendRequest.getMessageType()) && chatMessageSendRequest.getRoomId() != null) {
            try {
                ChatMessageVO messageVO = chatMessageService.getChatMessageVO(messageId, loginUser);
                ChatRoomWebSocketServer.broadcastNewMessage(chatMessageSendRequest.getRoomId(), messageVO, loginUser.getId());
            } catch (Exception e) {
                log.error("WebSocket广播消息失败", e);
                // 不影响HTTP响应，只记录日志
            }
        }
        
        return ResultUtils.success(messageId);
    }

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/recall/{messageId}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> recallMessage(@PathVariable Long messageId, HttpServletRequest request) {
        ThrowUtils.throwIf(messageId == null || messageId <= 0, ErrorCode.PARAMS_ERROR, "消息ID不能为空");
        
        User loginUser = userService.getLoginUser(request);
        
        // 先获取消息信息，用于WebSocket广播
        ChatMessageVO messageVO = null;
        try {
            messageVO = chatMessageService.getChatMessageVO(messageId, loginUser);
        } catch (Exception e) {
            log.warn("获取消息信息失败，messageId: {}", messageId, e);
        }
        
        boolean result = chatMessageService.recallMessage(messageId, loginUser);
        
        // 如果撤回成功且是房间消息，通过WebSocket广播
        if (result && messageVO != null && "room".equals(messageVO.getMessageType()) && messageVO.getRoomId() != null) {
            try {
                ChatRoomWebSocketServer.broadcastMessageRecalled(messageVO.getRoomId(), messageId, loginUser.getId());
            } catch (Exception e) {
                log.error("WebSocket广播消息撤回失败", e);
                // 不影响HTTP响应，只记录日志
            }
        }
        
        return ResultUtils.success(result);
    }

    /**
     * 分页获取房间消息
     *
     * @param chatMessageQueryRequest 查询请求
     * @param request HTTP请求
     * @return 消息列表
     */
    @PostMapping("/room/list/page")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Page<ChatMessageVO>> listRoomMessageVOByPage(@RequestBody ChatMessageQueryRequest chatMessageQueryRequest,
                                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(chatMessageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        Page<ChatMessageVO> chatMessageVOPage = chatMessageService.listRoomMessageVOByPage(chatMessageQueryRequest, loginUser);
        
        return ResultUtils.success(chatMessageVOPage);
    }

    /**
     * 分页获取私聊消息
     *
     * @param chatMessageQueryRequest 查询请求
     * @param request HTTP请求
     * @return 消息列表
     */
    @PostMapping("/private/list/page")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Page<ChatMessageVO>> listPrivateMessageVOByPage(@RequestBody ChatMessageQueryRequest chatMessageQueryRequest,
                                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(chatMessageQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        Page<ChatMessageVO> chatMessageVOPage = chatMessageService.listPrivateMessageVOByPage(chatMessageQueryRequest, loginUser);
        
        return ResultUtils.success(chatMessageVOPage);
    }

    /**
     * 获取消息详情
     *
     * @param messageId 消息ID
     * @param request HTTP请求
     * @return 消息详情
     */
    @GetMapping("/get/{messageId}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<ChatMessageVO> getChatMessage(@PathVariable Long messageId, HttpServletRequest request) {
        ThrowUtils.throwIf(messageId == null || messageId <= 0, ErrorCode.PARAMS_ERROR, "消息ID不能为空");
        
        User loginUser = userService.getLoginUser(request);
        ChatMessageVO chatMessageVO = chatMessageService.getChatMessageVO(messageId, loginUser);
        
        return ResultUtils.success(chatMessageVO);
    }
}