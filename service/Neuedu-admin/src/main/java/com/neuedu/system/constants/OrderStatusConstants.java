package com.neuedu.system.constants;

/**
 * 订单状态常量
 */
public class OrderStatusConstants {

    private OrderStatusConstants() {}

    // 订单状态

    public static final Integer WAIT_PAY = 10;        // 待付款
    public static final Integer WAIT_SHIP = 20;       // 待发货
    public static final Integer WAIT_RECEIVE = 30;    // 待收货
    public static final Integer WAIT_REVIEW = 40;     // 待评价
    public static final Integer COMPLETED = 50;       // 交易完成
    public static final Integer CANCELLED = 60;       // 已取消

    // 支付类型
    public static final Integer PAYMENT_ALIPAY = 1;   // 支付宝
    public static final Integer PAYMENT_WECHAT = 2;   // 微信支付
    public static final Integer PAYMENT_BANKCARD = 3; // 银行卡支付
    public static final Integer PAYMENT_BALANCE = 4;  // 余额支付

    // 退款状态
    public static final Integer REFUND_APPLYING = 1;  // 申请中
    public static final Integer REFUND_PROCESSING = 2;// 处理中
    public static final Integer REFUND_SUCCESS = 3;   // 退款成功
    public static final Integer REFUND_FAILED = 4;    // 退款失败
    public static final Integer REFUND_REJECTED = 5;  // 已拒绝

    /**
     * 获取订单状态描述
     */
    public static String getOrderStatusDesc(Integer status) {
        if (status == null) return "未知状态";

        switch (status) {
            case 10: return "待付款";
            case 20: return "待发货";
            case 30: return "待收货";
            case 40: return "待评价";
            case 50: return "交易完成";
            case 60: return "已取消";
            default: return "未知状态";
        }
    }

    /**
     * 获取支付类型描述
     */
    public static String getPaymentTypeDesc(Integer paymentType) {
        if (paymentType == null) return "未知支付方式";

        switch (paymentType) {
            case 1: return "支付宝";
            case 2: return "微信支付";
            case 3: return "银行卡支付";
            case 4: return "余额支付";
            default: return "未知支付方式";
        }
    }

    /**
     * 获取退款状态描述
     */
    public static String getRefundStatusDesc(Integer refundStatus) {
        if (refundStatus == null) return "未知状态";

        switch (refundStatus) {
            case 1: return "申请中";
            case 2: return "处理中";
            case 3: return "退款成功";
            case 4: return "退款失败";
            case 5: return "已拒绝";
            default: return "未知状态";
        }
    }
}