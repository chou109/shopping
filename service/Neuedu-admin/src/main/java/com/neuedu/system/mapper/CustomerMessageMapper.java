package com.neuedu.system.mapper;

import java.util.List;
import java.util.Map;

import com.neuedu.system.domain.CustomerMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 客服消息Mapper接口
 * 
 * @author neuedu
 * @date 2025-12-26
 */
@Mapper
public interface CustomerMessageMapper extends BaseMapper<CustomerMessage>
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
     * 删除客服消息
     * 
     * @param id 客服消息主键
     * @return 结果
     */
    int deleteCustomerMessageById(Long id);

    /**
     * 批量删除客服消息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteCustomerMessageByIds(Long[] ids);

    /**
     * 根据会话ID查询消息列表
     */
    List<CustomerMessage> selectMessagesBySessionId(@Param("sessionId") String sessionId,
                                                     @Param("status") Integer status);

    /**
     * 查询用户的会话列表（去重）
     */
    List<Map<String, Object>> selectUserSessionList(@Param("userId") Long userId);

    /**
     * 查询商家收到的所有会话列表（去重）
     */
    List<Map<String, Object>> selectAdminSessionList(@Param("adminId") Long adminId);

    /**
     * 统计用户未读消息数量
     */
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 统计商家未读消息数量
     */
    Long countUnreadByAdmin();

    /**
     * 统计会话未读消息数量
     */
    Long countUnreadBySessionId(@Param("sessionId") String sessionId,
                                @Param("receiverType") Integer receiverType);

    /**
     * 标记消息为已读
     */
    int markAsRead(@Param("sessionId") String sessionId,
                   @Param("receiverType") Integer receiverType);

    /**
     * 标记单条消息为已读
     */
    int markMessageAsRead(@Param("id") Long id);

    /**
     * 获取会话的最后一条消息
     */
    CustomerMessage selectLastMessageBySessionId(@Param("sessionId") String sessionId);

    /**
     * 生成会话ID（基于用户ID）
     */
    default String generateSessionId(Long userId) {
        return "CS_" + userId;
    }
}
