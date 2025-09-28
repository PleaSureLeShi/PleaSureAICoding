package com.pleasure.pleasureaicoding.service;

import com.mybatisflex.core.service.IService;
import com.pleasure.pleasureaicoding.model.entity.RoomMember;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.RoomMemberVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间成员服务接口
 */
public interface RoomMemberService extends IService<RoomMember> {

    /**
     * 用户加入房间
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean joinRoom(Long roomId, Long userId);

    /**
     * 用户退出房间
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveRoom(Long roomId, Long userId);

    /**
     * 检查用户是否已加入房间
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否已加入
     */
    boolean isUserInRoom(Long roomId, Long userId);

    /**
     * 获取房间成员列表
     *
     * @param roomId 房间ID
     * @return 成员列表
     */
    List<User> getRoomMembers(Long roomId);

    /**
     * 获取房间成员详细信息列表（包含角色信息）
     *
     * @param roomId 房间ID
     * @return 成员详细信息列表
     */
    List<RoomMemberVO> getRoomMembersWithRole(Long roomId);

    /**
     * 获取房间成员数量
     *
     * @param roomId 房间ID
     * @return 成员数量
     */
    int getRoomMemberCount(Long roomId);

    /**
     * 获取用户加入的房间列表
     *
     * @param userId 用户ID
     * @return 房间ID列表
     */
    List<Long> getUserJoinedRooms(Long userId);

    /**
     * 设置用户在房间中的角色
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param role 角色
     * @return 是否成功
     */
    boolean setUserRole(Long roomId, Long userId, String role);

    /**
     * 禁言用户
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @param mutedUntil 禁言到期时间
     * @return 是否成功
     */
    boolean muteUser(Long roomId, Long userId, LocalDateTime mutedUntil);

    /**
     * 取消禁言
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unmuteUser(Long roomId, Long userId);

    /**
     * 更新用户最后阅读时间
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateLastReadTime(Long roomId, Long userId);
}