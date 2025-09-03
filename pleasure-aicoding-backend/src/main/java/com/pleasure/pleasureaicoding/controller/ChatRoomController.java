package com.pleasure.pleasureaicoding.controller;

import com.mybatisflex.core.paginate.Page;
import com.pleasure.pleasureaicoding.annotation.AuthCheck;
import com.pleasure.pleasureaicoding.common.BaseResponse;
import com.pleasure.pleasureaicoding.common.ResultUtils;
import com.pleasure.pleasureaicoding.constant.UserConstant;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.exception.ThrowUtils;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomCreateRequest;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomJoinRequest;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomQueryRequest;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatRoomVO;
import com.pleasure.pleasureaicoding.service.ChatRoomService;
import com.pleasure.pleasureaicoding.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天房间接口
 */
@RestController
@RequestMapping("/chatRoom")
@Slf4j
public class ChatRoomController {

    @Resource
    private ChatRoomService chatRoomService;

    @Resource
    private UserService userService;

    /**
     * 创建房间
     *
     * @param chatRoomCreateRequest 创建请求
     * @param request HTTP请求
     * @return 房间ID
     */
    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Long> createChatRoom(@RequestBody ChatRoomCreateRequest chatRoomCreateRequest,
                                           HttpServletRequest request) {
        ThrowUtils.throwIf(chatRoomCreateRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        Long roomId = chatRoomService.createChatRoom(chatRoomCreateRequest, loginUser);
        
        return ResultUtils.success(roomId);
    }

    /**
     * 分页获取房间列表
     *
     * @param chatRoomQueryRequest 查询请求
     * @param request HTTP请求
     * @return 房间列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ChatRoomVO>> listChatRoomVOByPage(@RequestBody ChatRoomQueryRequest chatRoomQueryRequest,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(chatRoomQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUserPermitNull(request);
        Page<ChatRoomVO> chatRoomVOPage = chatRoomService.listChatRoomVOByPage(chatRoomQueryRequest, loginUser);
        
        return ResultUtils.success(chatRoomVOPage);
    }

    /**
     * 获取用户已加入的房间列表
     *
     * @param request HTTP请求
     * @return 房间列表
     */
    @GetMapping("/joined")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<ChatRoomVO>> getUserJoinedRooms(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<ChatRoomVO> joinedRooms = chatRoomService.getUserJoinedRooms(loginUser);
        
        return ResultUtils.success(joinedRooms);
    }

    /**
     * 加入房间
     *
     * @param chatRoomJoinRequest 加入请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/join")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> joinRoom(@RequestBody ChatRoomJoinRequest chatRoomJoinRequest,
                                        HttpServletRequest request) {
        ThrowUtils.throwIf(chatRoomJoinRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser(request);
        boolean result = chatRoomService.joinRoom(chatRoomJoinRequest, loginUser);
        
        return ResultUtils.success(result);
    }

    /**
     * 退出房间
     *
     * @param roomId 房间ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/leave/{roomId}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> leaveRoom(@PathVariable Long roomId, HttpServletRequest request) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        
        User loginUser = userService.getLoginUser(request);
        boolean result = chatRoomService.leaveRoom(roomId, loginUser);
        
        return ResultUtils.success(result);
    }

    /**
     * 获取房间详情
     *
     * @param roomId 房间ID
     * @param request HTTP请求
     * @return 房间详情
     */
    @GetMapping("/get/{roomId}")
    public BaseResponse<ChatRoomVO> getChatRoom(@PathVariable Long roomId, HttpServletRequest request) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        
        User loginUser = userService.getLoginUserPermitNull(request);
        ChatRoomVO chatRoomVO = chatRoomService.getChatRoomVO(roomId, loginUser);
        
        return ResultUtils.success(chatRoomVO);
    }
}