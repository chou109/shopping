package com.neuedu.system.service;

import java.util.List;
import java.util.Map;
import com.neuedu.system.domain.CustomerMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 客服消息Service接口
 * 
 * @author neuedu
 * @date 2025-12-26
 */
public interface ICustomerMessageService extends IService<CustomerMessage>
{
    /**
     * 查询客服消息
     * 
     * @param id 客服消息主键
     * @return 客服消息
     */
    CustomerMessage selectCustomerMessageById(Long id);

    /**
     * 查询客服消息列表
     * 
     * @param customerMessage 客服消息
     * @return 客服消息集合
     */
    List<CustomerMessage> selectCustomerMessageList(CustomerMessage customerMessage);

    /**
     * 新增客服消息
     * 
     * @param customerMessage 客服消息
     * @return 结果
     */
    int insertCustomerMessage(CustomerMessage customerMessage);

    /**
     * 修改客服消息
     * 
     * @param customerMessage 客服消息
     * @return 结果
     */
    int updateCustomerMessage(CustomerMessage customerMessage);

    /**
     * 批量删除客服消息
     * 
     * @param ids 需要删除的客服消息主键集合
     * @return 结果
     */
    int deleteCustomerMessageByIds(Long[] ids);

    /**
     * 删除客服消息信息
     * 
     * @param id 客服消息主键
     * @return 结果
     */
    int deleteCustomerMessageById(Long id);

    // ============ 业务方法 ============

    /**
     * 用户发送消息给商家
     * 
     * @param userId 用户ID
     * @param userName 用户名称
     * @param userAvatar 用户头像
     * @param content 消息内容
     * @param messageType 消息类型
     * @return 消息对象
     */
    CustomerMessage sendMessageToAdmin(Long userId, String userName, String userAvatar, 
                                        String content, Integer messageType);
    
    /**
     * 用户发送消息给指定店铺主人
     * 
     * @param userId 用户ID
     * @param userName 用户名称
     * @param userAvatar 用户头像
     * @param content 消息内容
     * @param messageType 消息类型
     * @param receiverId 店铺主人ID（接收者ID）
     * @return 消息对象
     */
    CustomerMessage sendMessageToAdmin(Long userId, String userName, String userAvatar, 
                                        String content, Integer messageType, Long receiverId);

    /**
     * 商家回复消息给用户
     * 
     * @param sessionId 会话ID
     * @param adminId 商家ID
     * @param adminName 商家名称
     * @param content 消息内容
     * @param messageType 消息类型
     * @return 消息对象
     */
    CustomerMessage sendMessageToUser(String sessionId, Long adminId, String adminName, 
                                       String content, Integer messageType);

    /**
     * 获取用户的会话消息列表
     * 
     * @param userId 用户ID
     * @return 消息列表
     */
    List<CustomerMessage> getUserMessages(Long userId);
    
    /**
     * 获取用户与指定接收者的消息列表
     * 
     * @param userId 用户ID
     * @param receiverId 接收者ID（店铺主人或管理员）
     * @return 消息列表
     */
    List<CustomerMessage> getUserMessages(Long userId, Long receiverId);

    /**
     * 获取指定会话的消息列表
     * 
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<CustomerMessage> getSessionMessages(String sessionId);

    /**
     * 获取用户的会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Map<String, Object>> getUserSessionList(Long userId);

    /**
     * 获取商家的所有会话列表（已废弃，使用带adminId的方法）
     * 
     * @return 会话列表
     */
    List<Map<String, Object>> getAdminSessionList();
    
    /**
     * 获取指定商家的会话列表（只获取发给该商家的消息）
     * 
     * @param adminId 商家/管理员ID
     * @return 会话列表
     */
    List<Map<String, Object>> getAdminSessionList(Long adminId);

    /**
     * 统计用户未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读数量
     */
    Long countUnreadByUserId(Long userId);

    /**
     * 统计商家未读消息数量
     * 
     * @return 未读数量
     */
    Long countUnreadByAdmin();

    /**
     * 标记会话消息为已读
     * 
     * @param sessionId 会话ID
     * @param receiverType 接收者类型（1-用户，2-商家）
     * @return 影响行数
     */
    int markAsRead(String sessionId, Integer receiverType);

    /**
     * 生成会话ID
     * 
     * @param userId 用户ID
     * @return 会话ID
     */
    String generateSessionId(Long userId);
}
