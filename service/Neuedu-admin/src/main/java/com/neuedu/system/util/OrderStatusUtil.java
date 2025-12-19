package com.neuedu.system.util;

/**
 * 订单状态工具类
 */
public class OrderStatusUtil {

    private OrderStatusUtil() {}

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        WAIT_PAY(10, "待付款"),
        PAID(20, "已付款"),
        WAIT_SHIP(30, "待发货"),
        SHIPPED(40, "已发货"),
        WAIT_RECEIVE(50, "待收货"),
        COMPLETED(60, "已完成"),
        CANCELLED(70, "已取消"),
        REFUNDING(80, "退款中"),
        REFUNDED(90, "已退款");

        private final Integer code;
        private final String desc;

        OrderStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 支付类型枚举
     */
    public enum PaymentType {
        ALIPAY(1, "支付宝"),
        WECHAT_PAY(2, "微信支付"),
        BALANCE(3, "余额支付");

        private final Integer code;
        private final String desc;

        PaymentType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 根据状态码获取状态描述
     */
    public static String getStatusDescByCode(Integer code) {
        if (code == null) {
            return "未知状态";
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }

    /**
     * 根据支付类型码获取支付类型描述
     */
    public static String getPaymentDescByCode(Integer code) {
        if (code == null) {
            return "未知支付方式";
        }
        for (PaymentType type : PaymentType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return "未知支付方式";
    }

    /**
     * 根据状态码获取状态枚举
     */
    public static OrderStatus getOrderStatusByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据支付类型码获取支付类型枚举
     */
    public static PaymentType getPaymentTypeByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentType type : PaymentType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查订单是否允许支付
     */
    public static boolean canPay(Integer status) {
        return OrderStatus.WAIT_PAY.getCode().equals(status);
    }

    /**
     * 检查订单是否允许取消
     */
    public static boolean canCancel(Integer status) {
        return OrderStatus.WAIT_PAY.getCode().equals(status)
                || OrderStatus.PAID.getCode().equals(status);
    }

    /**
     * 检查订单是否允许发货
     */
    public static boolean canShip(Integer status) {
        return OrderStatus.PAID.getCode().equals(status);
    }

    /**
     * 检查订单是否允许确认收货
     */
    public static boolean canConfirmReceipt(Integer status) {
        return OrderStatus.SHIPPED.getCode().equals(status);
    }

    /**
     * 检查订单是否允许退款
     */
    public static boolean canRefund(Integer status) {
        return OrderStatus.PAID.getCode().equals(status)
                || OrderStatus.SHIPPED.getCode().equals(status);
    }
}