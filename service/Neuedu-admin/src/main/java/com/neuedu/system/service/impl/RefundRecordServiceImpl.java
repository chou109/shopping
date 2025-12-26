package com.neuedu.system.service.impl;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.RefundRecordMapper;
import com.neuedu.system.domain.RefundRecord;
import com.neuedu.system.service.IRefundRecordService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 退款记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefundRecordServiceImpl extends ServiceImpl<RefundRecordMapper, RefundRecord> implements IRefundRecordService
{
    @Autowired
    private RefundRecordMapper refundRecordMapper;

    // 使用 @Lazy 注解延迟加载，解决循环依赖
    @Autowired
    @Lazy
    private IOrderInfoService orderInfoService;

    @Autowired
    private ISysUserService sysUserService;
    
    @Autowired
    private IProductService productService;
    /**
     * 查询退款记录
     * 
     * @param id 退款记录主键
     * @return 退款记录
     */
    @Override
    public RefundRecord selectRefundRecordById(Long id)
    {
        return refundRecordMapper.selectRefundRecordById(id);
    }

    /**
     * 查询退款记录列表
     * 
     * @param refundRecord 退款记录
     * @return 退款记录
     */
    @Override
    public List<RefundRecord> selectRefundRecordList(RefundRecord refundRecord)
    {
        return refundRecordMapper.selectRefundRecordList(refundRecord);
    }

    /**
     * 根据退款单号查询退款记录
     *
     * @param refundNo 退款单号
     * @return 退款记录
     */
    @Override
    public RefundRecord selectRefundRecordByRefundNo(String refundNo)
    {
        return refundRecordMapper.selectRefundRecordByRefundNo(refundNo);
    }

    /**
     * 新增退款记录
     * 
     * @param refundRecord 退款记录
     * @return 结果
     */
    @Override
    public int insertRefundRecord(RefundRecord refundRecord)
    {
        refundRecord.setCreateTime(DateUtils.getNowDate());
        return refundRecordMapper.insertRefundRecord(refundRecord);
    }

    /**
     * 修改退款记录
     * 
     * @param refundRecord 退款记录
     * @return 结果
     */
    @Override
    public int updateRefundRecord(RefundRecord refundRecord)
    {
        refundRecord.setUpdateTime(DateUtils.getNowDate());
        return refundRecordMapper.updateRefundRecord(refundRecord);
    }

    /**
     * 批量删除退款记录
     * 
     * @param ids 需要删除的退款记录主键
     * @return 结果
     */
    @Override
    public int deleteRefundRecordByIds(Long[] ids)
    {
        return refundRecordMapper.deleteRefundRecordByIds(ids);
    }

    /**
     * 删除退款记录信息
     * 
     * @param id 退款记录主键
     * @return 结果
     */
    @Override
    public int deleteRefundRecordById(Long id)
    {
        return refundRecordMapper.deleteRefundRecordById(id);
    }

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param userId 用户ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyRefund(String orderNo, Long userId, BigDecimal refundAmount, String refundReason)
    {
        // 1. 验证订单
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单是否属于该用户
        if (!orderInfo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 验证订单状态是否可以退款
        if (!OrderStatusConstants.PAID.equals(orderInfo.getStatus())
                && !OrderStatusConstants.SHIPPED.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法申请退款");
        }

        // 验证退款金额
        if (refundAmount.compareTo(orderInfo.getPayment()) > 0) {
            throw new RuntimeException("退款金额不能超过订单金额");
        }

        // 2. 生成退款单号
        String refundNo = generateRefundNo();

        // 3. 创建退款记录
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setRefundNo(refundNo);
        refundRecord.setOrderNo(orderNo);
        refundRecord.setUserId(userId);
        refundRecord.setRefundAmount(refundAmount);
        refundRecord.setRefundStatus(OrderStatusConstants.REFUND_APPLYING);
        refundRecord.setRefundStatusDesc(OrderStatusConstants.getRefundStatusDesc(OrderStatusConstants.REFUND_APPLYING));
        refundRecord.setRefundReason(refundReason);
        refundRecord.setDeleteFlag(0);
        refundRecord.setCreateTime(DateUtils.getNowDate());

        int result = refundRecordMapper.insertRefundRecord(refundRecord);

        // 4. 更新订单状态为退款中
        if (result > 0) {
            orderInfo.setStatus(OrderStatusConstants.REFUNDING);
            orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.REFUNDING));
            orderInfo.setRefundAmount(refundAmount);
            orderInfo.setRefundReason(refundReason);
            orderInfo.setUpdateTime(DateUtils.getNowDate());
            orderInfoService.updateOrderInfo(orderInfo);
        }

        return result;
    }

    /**
     * 处理退款
     *
     * @param refundNo 退款单号
     * @param refundStatus 退款状态
     * @param auditOpinion 审核意见
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(String refundNo, Integer refundStatus, String auditOpinion)
    {
        // 1. 获取退款记录
        RefundRecord refundRecord = refundRecordMapper.selectRefundRecordByRefundNo(refundNo);
        if (refundRecord == null) {
            throw new RuntimeException("退款记录不存在");
        }

        // 2. 更新退款记录状态
        refundRecord.setRefundStatus(refundStatus);
        refundRecord.setRefundStatusDesc(OrderStatusConstants.getRefundStatusDesc(refundStatus));
        refundRecord.setAuditOpinion(auditOpinion);
        refundRecord.setUpdateTime(DateUtils.getNowDate());

        // 如果是退款成功，设置退款时间
        if (OrderStatusConstants.REFUND_SUCCESS.equals(refundStatus)) {
            refundRecord.setRefundTime(DateUtils.getNowDate());

            // 3. 执行退款操作（根据退款方式）
            OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(refundRecord.getOrderNo());
            if (orderInfo != null) {
                // 更新订单状态为已退款
                orderInfo.setStatus(OrderStatusConstants.REFUNDED);
                orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.REFUNDED));
                orderInfo.setRefundTime(DateUtils.getNowDate());
                orderInfo.setUpdateTime(DateUtils.getNowDate());
                orderInfoService.updateOrderInfo(orderInfo);

                // 恢复商品库存
                if (orderInfo.getOrderDetails() != null) {
                    for (OrderDetail detail : orderInfo.getOrderDetails()) {
                        productService.updateProductStock(detail.getProductId(), Long.valueOf(detail.getQuantity()));
                    }
                }
                
                // 如果是余额支付，退还余额到 sys_user 表
                if (orderInfo.getPaymentType() != null && orderInfo.getPaymentType().equals(OrderStatusConstants.PAYMENT_BALANCE)) {
                    sysUserService.updateUserBalance(orderInfo.getUserId(), refundRecord.getRefundAmount());
                }
                // 其他支付方式的退款逻辑...
            }
        }

        return refundRecordMapper.updateRefundRecord(refundRecord);
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        // 格式：R + 年月日时分秒 + 6位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStr = LocalDateTime.now().format(formatter);
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "R" + timeStr + randomStr;
    }
}
