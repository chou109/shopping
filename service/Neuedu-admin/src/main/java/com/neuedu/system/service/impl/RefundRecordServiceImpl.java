package com.neuedu.system.service.impl;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.Product;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;
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

        // 验证订单状态是否可以退款（待发货或待收货）
        if (!OrderStatusConstants.WAIT_SHIP.equals(orderInfo.getStatus())
                && !OrderStatusConstants.WAIT_RECEIVE.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法申请退款");
        }

        // 检查订单中是否包含盲盒商品
        if (orderInfo.getOrderDetails() != null) {
            for (OrderDetail detail : orderInfo.getOrderDetails()) {
                if (detail.getProductId() != null) {
                    Product product = productService.selectProductById(detail.getProductId());
                    if (product != null && Integer.valueOf(1).equals(product.getIsBlindBox())) {
                        throw new RuntimeException("盲盒商品不支持退款");
                    }
                }
            }
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


        // 4. 更新订单状态为已取消
        if (result > 0) {
            orderInfo.setStatus(OrderStatusConstants.CANCELLED);
            orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.CANCELLED));
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
        if (refundNo == null || refundNo.trim().isEmpty()) {
            throw new RuntimeException("退款单号不能为空");
        }
        if (!OrderStatusConstants.REFUND_PROCESSING.equals(refundStatus)
                && !OrderStatusConstants.REFUND_SUCCESS.equals(refundStatus)
                && !OrderStatusConstants.REFUND_FAILED.equals(refundStatus)
                && !OrderStatusConstants.REFUND_REJECTED.equals(refundStatus)) {
            throw new RuntimeException("退款状态不合法");
        }

        // 1. 获取退款记录
        RefundRecord refundRecord = refundRecordMapper.selectRefundRecordByRefundNo(refundNo);
        if (refundRecord == null) {
            throw new RuntimeException("退款记录不存在");
        }

        OrderInfo orderInfo = null;
        if (OrderStatusConstants.REFUND_SUCCESS.equals(refundStatus)) {
            orderInfo = orderInfoService.selectOrderInfoByOrderNo(refundRecord.getOrderNo());
            if (orderInfo == null) {
                throw new RuntimeException("退款订单不存在");
            }
            if (!OrderStatusConstants.PAYMENT_BALANCE.equals(orderInfo.getPaymentType())) {
                throw new RuntimeException("当前仅支持余额支付订单退款，尚未接入原路退款");
            }
            if (refundRecord.getRefundAmount() == null || orderInfo.getPayment() == null
                    || refundRecord.getRefundAmount().compareTo(orderInfo.getPayment()) != 0) {
                throw new RuntimeException("当前订单仅支持全额退款");
            }
            if (orderInfo.getOrderDetails() == null || orderInfo.getOrderDetails().isEmpty()) {
                throw new RuntimeException("订单没有明细，无法恢复库存");
            }
        }

        Date refundTime = OrderStatusConstants.REFUND_SUCCESS.equals(refundStatus)
            ? DateUtils.getNowDate() : null;
        int transitioned = refundRecordMapper.transitionRefundStatus(
            refundNo, refundStatus, OrderStatusConstants.getRefundStatusDesc(refundStatus), auditOpinion, refundTime);
        if (transitioned == 0) {
            throw new RuntimeException("退款单已处理，请刷新后重试");
        }

        // 只有成功取得状态迁移的事务可执行余额和库存副作用。
        if (OrderStatusConstants.REFUND_SUCCESS.equals(refundStatus)) {
            orderInfo.setStatus(OrderStatusConstants.CANCELLED);
            orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.CANCELLED));
            orderInfo.setRefundTime(DateUtils.getNowDate());
            orderInfo.setUpdateTime(DateUtils.getNowDate());
            orderInfoService.updateOrderInfo(orderInfo);

            for (OrderDetail detail : orderInfo.getOrderDetails()) {
                productService.updateProductStock(detail.getProductId(), Long.valueOf(detail.getQuantity()));
            }

            sysUserService.updateUserBalance(orderInfo.getUserId(), refundRecord.getRefundAmount());
        }

        return transitioned;
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
