package com.neuedu.system.service;

import java.math.BigDecimal;
import java.util.List;
import com.neuedu.system.domain.RefundRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 退款记录Service接口
 * 
 * @author ruoyi
 * @date 2025-12-16
 */
public interface IRefundRecordService extends IService<RefundRecord>
{
    /**
     * 查询退款记录
     * 
     * @param id 退款记录主键
     * @return 退款记录
     */
    public RefundRecord selectRefundRecordById(Long id);

    /**
     * 查询退款记录列表
     *
     * @param refundRecord 退款记录
     * @return 退款记录集合
     */
    public List<RefundRecord> selectRefundRecordList(RefundRecord refundRecord);

    /**
     * 根据退款单号查询退款记录
     *
     * @param refundNo 退款单号
     * @return 退款记录
     */
    RefundRecord selectRefundRecordByRefundNo(String refundNo);


    /**
     * 新增退款记录
     * 
     * @param refundRecord 退款记录
     * @return 结果
     */
    public int insertRefundRecord(RefundRecord refundRecord);

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param userId 用户ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 结果
     */
    int applyRefund(String orderNo, Long userId, BigDecimal refundAmount, String refundReason);


    /**
     * 处理退款
     *
     * @param refundNo 退款单号
     * @param refundStatus 退款状态
     * @param auditOpinion 审核意见
     * @return 结果
     */
    int processRefund(String refundNo, Integer refundStatus, String auditOpinion);

    /**
     * 修改退款记录
     * 
     * @param refundRecord 退款记录
     * @return 结果
     */
    public int updateRefundRecord(RefundRecord refundRecord);

    /**
     * 批量删除退款记录
     * 
     * @param ids 需要删除的退款记录主键集合
     * @return 结果
     */
    public int deleteRefundRecordByIds(Long[] ids);

    /**
     * 删除退款记录信息
     * 
     * @param id 退款记录主键
     * @return 结果
     */
    public int deleteRefundRecordById(Long id);


}
