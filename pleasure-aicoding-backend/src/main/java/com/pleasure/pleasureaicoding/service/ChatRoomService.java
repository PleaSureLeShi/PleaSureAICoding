package com.pleasure.pleasureaicoding.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomCreateRequest;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomJoinRequest;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomQueryRequest;
import com.pleasure.pleasureaicoding.model.entity.ChatRoom;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatRoomVO;

import java.util.List;

/**
 * 聊天房间服务接口
 */
public interface ChatRoomService extends IService<ChatRoom> {

    /**
     * 创建房间
     *
     * @param chatRoomCreateRequest 创建请求
     * @param loginUser 登录用户
     * @return 房间ID
     */
    Long createChatRoom(ChatRoomCreateRequest chatRoomCreateRequest, User loginUser);

    /**
     * 分页获取房间列表
     *
     * @param chatRoomQueryRequest 查询请求
     * @param loginUser 登录用户
     * @return 房间列表
     */
    Page<ChatRoomVO> listChatRoomVOByPage(ChatRoomQueryRequest chatRoomQueryRequest, User loginUser);

    /**
     * 获取用户已加入的房间列表
     *
     * @param loginUser 登录用户
     * @return 房间列表
     */
    List<ChatRoomVO> getUserJoinedRooms(User loginUser);

    /**
     * 加入房间
     *
     * @param chatRoomJoinRequest 加入请求
     * @param loginUser 登录用户
     * @return 是否成功
     */
    boolean joinRoom(ChatRoomJoinRequest chatRoomJoinRequest, User loginUser);

    /**
     * 退出房间
     *
     * @param roomId 房间ID
     * @param loginUser 登录用户
     * @return 是否成功
     */
    boolean leaveRoom(Long roomId, User loginUser);

    /**
     * 获取房间详情
     *
     * @param roomId 房间ID
     * @param loginUser 登录用户
     * @return 房间详情
     */
    ChatRoomVO getChatRoomVO(Long roomId, User loginUser);

    /**
     * 更新房间成员数
     *
     * @param roomId 房间ID
     */
    void updateRoomMemberCount(Long roomId);
}