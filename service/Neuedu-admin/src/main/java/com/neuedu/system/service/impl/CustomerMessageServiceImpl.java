package com.neuedu.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.CustomerMessageMapper;
import com.neuedu.system.domain.CustomerMessage;
import com.neuedu.system.service.ICustomerMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;

/**
 * 客服消息Service业务层处理
 * 
 * @author neuedu
 * @date 2025-12-26
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CustomerMessageServiceImpl extends ServiceImpl<CustomerMessageMapper, CustomerMessage> implements ICustomerMessageService
{
    private final CustomerMessageMapper customerMessageMapper;

    /**
     * 查询客服消息
     * 
     * @param id 客服消息主键
     * @return 客服消息
     */
    @Override
    public CustomerMessage selectCustomerMessageById(Long id)
    {
        return customerMessageMapper.selectCustomerMessageById(id);
    }

    /**
     * 查询客服消息列表
     * 
     * @param customerMessage 客服消息
     * @return 客服消息
     */
    @Override
    public List<CustomerMessage> selectCustomerMessageList(CustomerMessage customerMessage)
    {
        return customerMessageMapper.selectCustomerMessageList(customerMessage);
    }

    /**
     * 新增客服消息
     * 
     * @param customerMessage 客服消息
     * @return 结果
     */
    @Override
    public int insertCustomerMessage(CustomerMessage customerMessage)
    {
        customerMessage.setCreateTime(DateUtils.getNowDate());
        return customerMessageMapper.insertCustomerMessage(customerMessage);
    }

    /**
     * 修改客服消息
     * 
     * @param customerMessage 客服消息
     * @return 结果
     */
    @Override
    public int updateCustomerMessage(CustomerMessage customerMessage)
    {
        customerMessage.setUpdateTime(DateUtils.getNowDate());
        return customerMessageMapper.updateCustomerMessage(customerMessage);
    }

    /**
     * 批量删除客服消息
     * 
     * @param ids 需要删除的客服消息主键
     * @return 结果
     */
    @Override
    public int deleteCustomerMessageByIds(Long[] ids)
    {
        return customerMessageMapper.deleteCustomerMessageByIds(ids);
    }

    /**
     * 删除客服消息信息
     * 
     * @param id 客服消息主键
     * @return 结果
     */
    @Override
    public int deleteCustomerMessageById(Long id)
    {
        return customerMessageMapper.deleteCustomerMessageById(id);
    }

    // ============ 业务方法实现 ============

    /**
     * 用户发送消息给商家
     */
    @Override
    public CustomerMessage sendMessageToAdmin(Long userId, String userName, String userAvatar,
                                               String content, Integer messageType) {
        return sendMessageToAdmin(userId, userName, userAvatar, content, messageType, null);
    }
    
    /**
     * 用户发送消息给指定店铺主人
     */
    @Override
    public CustomerMessage sendMessageToAdmin(Long userId, String userName, String userAvatar,
                                               String content, Integer messageType, Long receiverId) {
        CustomerMessage message = new CustomerMessage();
        // 如果指定了receiverId，生成包含receiverId的会话ID
        if (receiverId != null && receiverId > 0) {
            message.setSessionId("CS_" + userId + "_TO_" + receiverId);
            message.setReceiverId(receiverId);
        } else {
            message.setSessionId(generateSessionId(userId));
        }
        message.setSenderId(userId);
        message.setSenderType(CustomerMessage.SENDER_TYPE_USER);
        message.setSenderName(userName);
        message.setSenderAvatar(userAvatar);
        message.setReceiverType(CustomerMessage.SENDER_TYPE_ADMIN);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : CustomerMessage.MESSAGE_TYPE_TEXT);
        message.setIsRead(0);
        message.setStatus(CustomerMessage.STATUS_NORMAL);
        message.setCreateTime(DateUtils.getNowDate());
        
        customerMessageMapper.insertCustomerMessage(message);
        return message;
    }

    /**
     * 商家回复消息给用户
     */
    @Override
    public CustomerMessage sendMessageToUser(String sessionId, Long adminId, String adminName,
                                              String content, Integer messageType) {
        // 从sessionId中提取用户ID
        Long userId = extractUserIdFromSession(sessionId);
        
        CustomerMessage message = new CustomerMessage();
        message.setSessionId(sessionId);
        message.setSenderId(adminId);
        message.setSenderType(CustomerMessage.SENDER_TYPE_ADMIN);
        message.setSenderName(adminName != null ? adminName : "客服");
        message.setReceiverId(userId);
        message.setReceiverType(CustomerMessage.SENDER_TYPE_USER);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : CustomerMessage.MESSAGE_TYPE_TEXT);
        message.setIsRead(0);
        message.setStatus(CustomerMessage.STATUS_NORMAL);
        message.setCreateTime(DateUtils.getNowDate());
        
        customerMessageMapper.insertCustomerMessage(message);
        return message;
    }

    /**
     * 获取用户的会话消息列表
     */
    @Override
    public List<CustomerMessage> getUserMessages(Long userId) {
        return getUserMessages(userId, null);
    }
    
    /**
     * 获取用户与指定接收者的消息列表
     */
    @Override
    public List<CustomerMessage> getUserMessages(Long userId, Long receiverId) {
        String sessionId;
        if (receiverId != null && receiverId > 0) {
            // 根据用户ID和接收者ID生成特定会话ID
            sessionId = "CS_" + userId + "_TO_" + receiverId;
        } else {
            sessionId = generateSessionId(userId);
        }
        return customerMessageMapper.selectMessagesBySessionId(sessionId, CustomerMessage.STATUS_NORMAL);
    }

    /**
     * 获取指定会话的消息列表
     */
    @Override
    public List<CustomerMessage> getSessionMessages(String sessionId) {
        return customerMessageMapper.selectMessagesBySessionId(sessionId, CustomerMessage.STATUS_NORMAL);
    }

    @Override
    public List<CustomerMessage> getAdminSessionMessages(String sessionId, Long adminId) {
        if (adminId == null) return getSessionMessages(sessionId);
        return customerMessageMapper.selectAdminSessionMessages(sessionId, CustomerMessage.STATUS_NORMAL, adminId);
    }

    @Override
    public boolean isAdminSessionOwned(String sessionId, Long adminId) {
        return adminId == null || customerMessageMapper.existsAdminSessionForReceiver(sessionId, adminId);
    }

    /**
     * 获取用户的会话列表
     */
    @Override
    public List<Map<String, Object>> getUserSessionList(Long userId) {
        return customerMessageMapper.selectUserSessionList(userId);
    }

    /**
     * 获取商家的所有会话列表（已废弃）
     */
    @Override
    public List<Map<String, Object>> getAdminSessionList() {
        return getAdminSessionList(null);
    }
    
    /**
     * 获取指定商家的会话列表（只获取发给该商家的消息）
     */
    @Override
    public List<Map<String, Object>> getAdminSessionList(Long adminId) {
        return customerMessageMapper.selectAdminSessionList(adminId);
    }

    /**
     * 统计用户未读消息数量
     */
    @Override
    public Long countUnreadByUserId(Long userId) {
        return customerMessageMapper.countUnreadByUserId(userId);
    }

    /**
     * 统计商家未读消息数量
     */
    @Override
    public Long countUnreadByAdmin(Long adminId) {
        return customerMessageMapper.countUnreadByAdmin(adminId);
    }

    /**
     * 标记会话消息为已读
     */
    @Override
    public int markAsRead(String sessionId, Integer receiverType) {
        return customerMessageMapper.markAsRead(sessionId, receiverType, null);
    }

    @Override
    public int markAdminSessionAsRead(String sessionId, Long adminId) {
        if (adminId != null && !isAdminSessionOwned(sessionId, adminId)) return 0;
        return customerMessageMapper.markAsRead(sessionId, CustomerMessage.SENDER_TYPE_ADMIN, adminId);
    }

    /**
     * 生成会话ID
     */
    @Override
    public String generateSessionId(Long userId) {
        return "CS_" + userId;
    }

    /**
     * 从会话ID中提取用户ID
     * 支持格式: CS_{userId} 或 CS_{userId}_TO_{receiverId}
     */
    private Long extractUserIdFromSession(String sessionId) {
        if (sessionId != null && sessionId.startsWith("CS_")) {
            try {
                String idPart = sessionId.substring(3);
                // 处理新格式 CS_{userId}_TO_{receiverId}
                if (idPart.contains("_TO_")) {
                    idPart = idPart.split("_TO_")[0];
                }
                return Long.parseLong(idPart);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
