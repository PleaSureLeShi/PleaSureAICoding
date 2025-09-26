package com.pleasure.pleasureaicoding.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pleasure.pleasureaicoding.constant.UserConstant;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.exception.ThrowUtils;
import com.pleasure.pleasureaicoding.mapper.ChatRoomMapper;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomCreateRequest;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomJoinRequest;
import com.pleasure.pleasureaicoding.model.dto.chatroom.ChatRoomQueryRequest;
import com.pleasure.pleasureaicoding.model.entity.ChatRoom;
import com.pleasure.pleasureaicoding.model.entity.RoomMember;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatRoomVO;
import com.pleasure.pleasureaicoding.service.ChatRoomService;
import com.pleasure.pleasureaicoding.service.RoomMemberService;
import com.pleasure.pleasureaicoding.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pleasure.pleasureaicoding.model.entity.table.ChatRoomTableDef.CHAT_ROOM;
import static com.pleasure.pleasureaicoding.model.entity.table.RoomMemberTableDef.ROOM_MEMBER;
import static com.pleasure.pleasureaicoding.model.entity.table.UserTableDef.USER;

/**
 * 聊天房间服务实现类
 */
@Service
@Slf4j
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {

    @Resource
    private RoomMemberService roomMemberService;

    @Resource
    private UserService userService;

    @Override
    @Transactional
    public Long createChatRoom(ChatRoomCreateRequest chatRoomCreateRequest, User loginUser) {
        ThrowUtils.throwIf(chatRoomCreateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        String roomName = chatRoomCreateRequest.getRoomName();
        String roomType = chatRoomCreateRequest.getRoomType();
        Integer maxMembers = chatRoomCreateRequest.getMaxMembers();
        Integer isPublic = chatRoomCreateRequest.getIsPublic();
        String password = chatRoomCreateRequest.getPassword();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(roomName), ErrorCode.PARAMS_ERROR, "房间名称不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(roomType), ErrorCode.PARAMS_ERROR, "房间类型不能为空");
        ThrowUtils.throwIf(maxMembers == null || maxMembers < 2 || maxMembers > 500, 
                ErrorCode.PARAMS_ERROR, "最大成员数必须在2-500之间");
        ThrowUtils.throwIf(isPublic == null || (isPublic != 0 && isPublic != 1), 
                ErrorCode.PARAMS_ERROR, "房间类型参数错误");
        
        // 私有房间必须设置密码
        if (isPublic == 0) {
            ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR, "私有房间必须设置密码");
        }

        // 检查房间名称是否重复
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(CHAT_ROOM.ROOM_NAME.eq(roomName))
                .and(CHAT_ROOM.IS_DELETE.eq(0));
        boolean exists = this.exists(queryWrapper);
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "房间名称已存在");

        // 创建房间
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .roomDescription(chatRoomCreateRequest.getRoomDescription())
                .roomType(roomType)
                .maxMembers(maxMembers)
                .currentMembers(1) // 创建者自动加入
                .ownerId(loginUser.getId())
                .isPublic(isPublic)
                .password(isPublic == 0 ? password : null)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        log.info("Attempting to save chatRoom: {}", chatRoom);

        boolean saveResult;
        try {
            saveResult = this.save(chatRoom);
            log.info("Save result: {}", saveResult);
        } catch (Exception e) {
            log.error("Error saving chatRoom: {}", e.getMessage(), e);
            throw new RuntimeException("创建房间失败: " + e.getMessage());
        }

        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "创建房间失败");

        // 创建者自动加入房间
        boolean joinResult = roomMemberService.joinRoom(chatRoom.getId(), loginUser.getId());
        if (joinResult) {
            // 设置创建者为房主
            roomMemberService.setUserRole(chatRoom.getId(), loginUser.getId(), "owner");
        }

        return chatRoom.getId();
    }

    @Override
    public Page<ChatRoomVO> listChatRoomVOByPage(ChatRoomQueryRequest chatRoomQueryRequest, User loginUser) {
        ThrowUtils.throwIf(chatRoomQueryRequest == null, ErrorCode.PARAMS_ERROR);
        
        long pageNum = chatRoomQueryRequest.getPageNum();
        long pageSize = chatRoomQueryRequest.getPageSize();
        
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(CHAT_ROOM.ALL_COLUMNS, USER.USER_NAME.as("ownerName"))
                .from(CHAT_ROOM)
                .leftJoin(USER).on(CHAT_ROOM.OWNER_ID.eq(USER.ID))
                .where(CHAT_ROOM.IS_DELETE.eq(0))
                .and(CHAT_ROOM.STATUS.eq(1)) // 只显示正常状态的房间
                .orderBy(CHAT_ROOM.CREATE_TIME, false);

        // 添加查询条件
        String roomName = chatRoomQueryRequest.getRoomName();
        if (StrUtil.isNotBlank(roomName)) {
            queryWrapper.and(CHAT_ROOM.ROOM_NAME.like(roomName));
        }
        
        String roomType = chatRoomQueryRequest.getRoomType();
        if (StrUtil.isNotBlank(roomType)) {
            queryWrapper.and(CHAT_ROOM.ROOM_TYPE.eq(roomType));
        }
        
        Integer isPublic = chatRoomQueryRequest.getIsPublic();
        if (isPublic != null) {
            queryWrapper.and(CHAT_ROOM.IS_PUBLIC.eq(isPublic));
        }

        // 分页查询
        Page<ChatRoom> chatRoomPage = this.pageAs(Page.of(pageNum, pageSize), queryWrapper, ChatRoom.class);
        
        // 转换为VO
        return getChatRoomVOPage(chatRoomPage, loginUser);
    }

    @Override
    public List<ChatRoomVO> getUserJoinedRooms(User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 查询用户加入的房间
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(CHAT_ROOM.ALL_COLUMNS, USER.USER_NAME.as("ownerName"))
                .from(ROOM_MEMBER)
                .leftJoin(CHAT_ROOM).on(ROOM_MEMBER.ROOM_ID.eq(CHAT_ROOM.ID))
                .leftJoin(USER).on(CHAT_ROOM.OWNER_ID.eq(USER.ID))
                .where(ROOM_MEMBER.USER_ID.eq(loginUser.getId()))
                .and(ROOM_MEMBER.IS_DELETE.eq(0))
                .and(CHAT_ROOM.IS_DELETE.eq(0))
                .orderBy(ROOM_MEMBER.JOIN_TIME, false);

        // 直接查询为VO对象，保留ownerName字段
        List<ChatRoomVO> chatRoomVOs = this.listAs(queryWrapper, ChatRoomVO.class);
        
        // 设置已加入标志
        return chatRoomVOs.stream().map(chatRoomVO -> {
            chatRoomVO.setIsJoined(true); // 已加入的房间
            return chatRoomVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean joinRoom(ChatRoomJoinRequest chatRoomJoinRequest, User loginUser) {
        ThrowUtils.throwIf(chatRoomJoinRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long roomId = chatRoomJoinRequest.getRoomId();
        String password = chatRoomJoinRequest.getPassword();
        
        // 获取房间信息
        ChatRoom chatRoom = this.getById(roomId);
        ThrowUtils.throwIf(chatRoom == null, ErrorCode.NOT_FOUND_ERROR, "房间不存在");
        ThrowUtils.throwIf(chatRoom.getStatus() != 1, ErrorCode.FORBIDDEN_ERROR, "房间已被封禁");
        
        // 检查是否已加入
        boolean isJoined = roomMemberService.isUserInRoom(roomId, loginUser.getId());
        ThrowUtils.throwIf(isJoined, ErrorCode.PARAMS_ERROR, "您已经在该房间中");
        
        // 检查房间是否已满
        int currentMembers = roomMemberService.getRoomMemberCount(roomId);
        ThrowUtils.throwIf(currentMembers >= chatRoom.getMaxMembers(), 
                ErrorCode.FORBIDDEN_ERROR, "房间人数已满");
        
        // 私有房间需要验证密码
        if (chatRoom.getIsPublic() == 0) {
            ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR, "私有房间需要输入密码");
            ThrowUtils.throwIf(!password.equals(chatRoom.getPassword()), 
                    ErrorCode.FORBIDDEN_ERROR, "房间密码错误");
        }
        
        // 加入房间
        boolean joinResult = roomMemberService.joinRoom(roomId, loginUser.getId());
        if (joinResult) {
            // 更新房间成员数
            updateRoomMemberCount(roomId);
        }
        
        return joinResult;
    }

    @Override
    @Transactional
    public boolean leaveRoom(Long roomId, User loginUser) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 检查是否在房间中
        boolean isJoined = roomMemberService.isUserInRoom(roomId, loginUser.getId());
        ThrowUtils.throwIf(!isJoined, ErrorCode.PARAMS_ERROR, "您不在该房间中");
        
        // 获取房间信息
        ChatRoom chatRoom = this.getById(roomId);
        ThrowUtils.throwIf(chatRoom == null, ErrorCode.NOT_FOUND_ERROR, "房间不存在");
        
        // 房主不能退出房间
        ThrowUtils.throwIf(chatRoom.getOwnerId().equals(loginUser.getId()), 
                ErrorCode.FORBIDDEN_ERROR, "房主不能退出房间");
        
        // 退出房间
        boolean leaveResult = roomMemberService.leaveRoom(roomId, loginUser.getId());
        if (leaveResult) {
            // 更新房间成员数
            updateRoomMemberCount(roomId);
        }
        
        return leaveResult;
    }

    @Override
    public ChatRoomVO getChatRoomVO(Long roomId, User loginUser) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        
        // 查询房间信息
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(CHAT_ROOM.ALL_COLUMNS, USER.USER_NAME.as("ownerName"))
                .from(CHAT_ROOM)
                .leftJoin(USER).on(CHAT_ROOM.OWNER_ID.eq(USER.ID))
                .where(CHAT_ROOM.ID.eq(roomId))
                .and(CHAT_ROOM.IS_DELETE.eq(0));

        ChatRoom chatRoom = this.getOneAs(queryWrapper, ChatRoom.class);
        ThrowUtils.throwIf(chatRoom == null, ErrorCode.NOT_FOUND_ERROR, "房间不存在");
        
        // 转换为VO
        ChatRoomVO chatRoomVO = new ChatRoomVO();
        BeanUtils.copyProperties(chatRoom, chatRoomVO);
        
        // 检查用户是否已加入
        if (loginUser != null) {
            boolean isJoined = roomMemberService.isUserInRoom(roomId, loginUser.getId());
            chatRoomVO.setIsJoined(isJoined);
        } else {
            chatRoomVO.setIsJoined(false);
        }
        
        return chatRoomVO;
    }

    @Override
    public void updateRoomMemberCount(Long roomId) {
        int memberCount = roomMemberService.getRoomMemberCount(roomId);
        
        ChatRoom updateRoom = ChatRoom.builder()
                .id(roomId)
                .currentMembers(memberCount)
                .updateTime(LocalDateTime.now())
                .build();
        
        this.updateById(updateRoom);
    }

    /**
     * 转换为ChatRoomVO分页对象
     */
    private Page<ChatRoomVO> getChatRoomVOPage(Page<ChatRoom> chatRoomPage, User loginUser) {
        List<ChatRoom> chatRoomList = chatRoomPage.getRecords();
        if (chatRoomList.isEmpty()) {
            return new Page<>(chatRoomPage.getPageNumber(), chatRoomPage.getPageSize(), chatRoomPage.getTotalRow());
        }
        
        // 获取用户已加入的房间ID集合
        Set<Long> joinedRoomIds = Set.of();
        if (loginUser != null) {
            List<Long> userJoinedRooms = roomMemberService.getUserJoinedRooms(loginUser.getId());
            joinedRoomIds = userJoinedRooms.stream().collect(Collectors.toSet());
        }
        
        // 获取所有房主的用户信息
        Set<Long> ownerIds = chatRoomList.stream()
                .map(ChatRoom::getOwnerId)
                .collect(Collectors.toSet());
        Map<Long, String> ownerNameMap = new HashMap<>();
        if (!ownerIds.isEmpty()) {
            List<User> owners = userService.listByIds(ownerIds);
            ownerNameMap = owners.stream()
                    .collect(Collectors.toMap(User::getId, User::getUserName));
        }
        
        // 转换为VO
        final Set<Long> finalJoinedRoomIds = joinedRoomIds;
        final Map<Long, String> finalOwnerNameMap = ownerNameMap;
        List<ChatRoomVO> chatRoomVOList = chatRoomList.stream().map(chatRoom -> {
            ChatRoomVO chatRoomVO = new ChatRoomVO();
            BeanUtils.copyProperties(chatRoom, chatRoomVO);
            chatRoomVO.setIsJoined(finalJoinedRoomIds.contains(chatRoom.getId()));
            // 设置房主名称
            chatRoomVO.setOwnerName(finalOwnerNameMap.get(chatRoom.getOwnerId()));
            return chatRoomVO;
        }).collect(Collectors.toList());
        
        Page<ChatRoomVO> chatRoomVOPage = new Page<>(chatRoomPage.getPageNumber(), 
                chatRoomPage.getPageSize(), chatRoomPage.getTotalRow());
        chatRoomVOPage.setRecords(chatRoomVOList);
        return chatRoomVOPage;
    }
}