package com.pleasure.pleasureaicoding.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.exception.ThrowUtils;
import com.pleasure.pleasureaicoding.mapper.RoomMemberMapper;
import com.pleasure.pleasureaicoding.model.entity.RoomMember;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.RoomMemberVO;
import com.pleasure.pleasureaicoding.service.RoomMemberService;
import com.pleasure.pleasureaicoding.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.pleasure.pleasureaicoding.model.entity.table.RoomMemberTableDef.ROOM_MEMBER;
import static com.pleasure.pleasureaicoding.model.entity.table.UserTableDef.USER;

/**
 * 房间成员服务实现类
 */
@Service
@Slf4j
public class RoomMemberServiceImpl extends ServiceImpl<RoomMemberMapper, RoomMember> implements RoomMemberService {

    @Resource
    private UserService userService;

    @Override
    @Transactional
    public boolean joinRoom(Long roomId, Long userId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        try {
            // 检查是否已经加入
            if (isUserInRoom(roomId, userId)) {
                return true;
            }
    
            // 检查用户是否曾经加入过但已退出(isDelete=1)
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                    .and(ROOM_MEMBER.USER_ID.eq(userId));
            
            RoomMember existingMember = this.getOne(queryWrapper);
            if (existingMember != null) {
                // 如果用户曾经加入过但已退出，则更新isDelete为0
                existingMember.setIsDelete(0);
                existingMember.setJoinTime(LocalDateTime.now());
                existingMember.setLastReadTime(LocalDateTime.now());
                return this.updateById(existingMember);
            }
    
            // 创建房间成员记录
            RoomMember roomMember = RoomMember.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .role("member")
                    .joinTime(LocalDateTime.now())
                    .lastReadTime(LocalDateTime.now())
                    .isMuted(0)
                    .isDelete(0)
                    .build();

            return this.save(roomMember);
        } catch (Exception e) {
            // 处理唯一约束冲突异常
            log.error("加入房间失败，可能是并发操作导致的唯一约束冲突: roomId={}, userId={}", roomId, userId, e);
            
            // 再次检查是否已经加入，如果已加入则返回成功
            if (isUserInRoom(roomId, userId)) {
                return true;
            }
            try {
                QueryWrapper queryWrapper = QueryWrapper.create()
                        .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                        .and(ROOM_MEMBER.USER_ID.eq(userId));
                    
                RoomMember existingMember = this.getOne(queryWrapper);
                if (existingMember != null) {
                    existingMember.setIsDelete(0);
                    existingMember.setJoinTime(LocalDateTime.now());
                    existingMember.setLastReadTime(LocalDateTime.now());
                    return this.updateById(existingMember);
                }
            } catch (Exception ex) {
                log.error("尝试更新已存在的记录失败: roomId={}, userId={}", roomId, userId, ex);
            }
            
            // 其他异常则抛出
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean leaveRoom(Long roomId, Long userId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));
    
        RoomMember roomMember = this.getOne(queryWrapper);
        if (roomMember != null) {
            roomMember.setIsDelete(1);
            return this.updateById(roomMember);
        }
        return false;
    }

    @Override
    public boolean isUserInRoom(Long roomId, Long userId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        return this.exists(queryWrapper);
    }

    @Override
    public List<User> getRoomMembers(Long roomId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(USER.ALL_COLUMNS)
                .from(ROOM_MEMBER)
                .leftJoin(USER).on(ROOM_MEMBER.USER_ID.eq(USER.ID))
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0))
                .and(USER.IS_DELETE.eq(0));

        return userService.listAs(queryWrapper, User.class);
    }

    @Override
    public List<RoomMemberVO> getRoomMembersWithRole(Long roomId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");

        // 先获取房间成员信息
        QueryWrapper roomMemberQuery = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0))
                .orderBy(ROOM_MEMBER.ROLE.asc(), ROOM_MEMBER.JOIN_TIME.asc());

        List<RoomMember> roomMembers = this.list(roomMemberQuery);
        
        return roomMembers.stream()
                .filter(roomMember -> roomMember.getUserId() != null) // 过滤掉userId为null的记录
                .map(roomMember -> {
                    User user = userService.getById(roomMember.getUserId());
                    if (user == null || user.getIsDelete() == 1) {
                        return null;
                    }
                    return RoomMemberVO.builder()
                            .id(user.getId())
                            .userName(user.getUserName())
                            .userAvatar(user.getUserAvatar())
                            .userProfile(user.getUserProfile())
                            .role(roomMember.getRole())
                            .joinTime(roomMember.getJoinTime())
                            .lastReadTime(roomMember.getLastReadTime())
                            .isMuted(roomMember.getIsMuted())
                            .mutedUntil(roomMember.getMutedUntil())
                            .build();
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    @Override
    public int getRoomMemberCount(Long roomId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public List<Long> getUserJoinedRooms(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(ROOM_MEMBER.ROOM_ID)
                .where(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        List<RoomMember> roomMembers = this.list(queryWrapper);
        return roomMembers.stream()
                .map(RoomMember::getRoomId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean setUserRole(Long roomId, Long userId, String role) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(role == null || role.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "角色不能为空");

        RoomMember roomMember = RoomMember.builder()
                .role(role)
                .build();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        return this.update(roomMember, queryWrapper);
    }

    @Override
    @Transactional
    public boolean muteUser(Long roomId, Long userId, LocalDateTime mutedUntil) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        ThrowUtils.throwIf(mutedUntil == null, ErrorCode.PARAMS_ERROR, "禁言到期时间不能为空");

        RoomMember roomMember = RoomMember.builder()
                .isMuted(1)
                .mutedUntil(mutedUntil)
                .build();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        return this.update(roomMember, queryWrapper);
    }

    @Override
    @Transactional
    public boolean unmuteUser(Long roomId, Long userId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        RoomMember roomMember = RoomMember.builder()
                .isMuted(0)
                .mutedUntil(null)
                .build();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        return this.update(roomMember, queryWrapper);
    }

    @Override
    @Transactional
    public boolean updateLastReadTime(Long roomId, Long userId) {
        ThrowUtils.throwIf(roomId == null || roomId <= 0, ErrorCode.PARAMS_ERROR, "房间ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        RoomMember roomMember = RoomMember.builder()
                .lastReadTime(LocalDateTime.now())
                .build();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROOM_MEMBER.ROOM_ID.eq(roomId))
                .and(ROOM_MEMBER.USER_ID.eq(userId))
                .and(ROOM_MEMBER.IS_DELETE.eq(0));

        return this.update(roomMember, queryWrapper);
    }
}