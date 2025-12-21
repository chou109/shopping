package com.neuedu.system.constants;

/**
 * 充值相关常量
 */
public class RechargeConstants {

    /**
     * 充值状态
     */
    public static final class RechargeStatus {
        /** 待支付 */
        public static final int WAIT_PAY = 1;
        /** 支付成功 */
        public static final int SUCCESS = 2;
        /** 支付失败 */
        public static final int FAILED = 3;
        /** 已退款 */
        public static final int REFUNDED = 4;

        public static String getStatusDesc(Integer status) {
            if (status == null) return "未知";
            switch (status) {
                case WAIT_PAY: return "待支付";
                case SUCCESS: return "支付成功";
                case FAILED: return "支付失败";
                case REFUNDED: return "已退款";
                default: return "未知";
            }
        }
    }

    /**
     * 支付方式
     */
    public static final class PaymentType {
        /** 支付宝 */
        public static final int ALIPAY = 1;
        /** 微信支付 */
        public static final int WECHAT = 2;
        /** 余额支付 */
        public static final int BALANCE = 3;
        /** 银行卡 */
        public static final int BANK_CARD = 4;

        public static String getTypeDesc(Integer type) {
            if (type == null) return "未知";
            switch (type) {
                case ALIPAY: return "支付宝";
                case WECHAT: return "微信支付";
                case BALANCE: return "余额支付";
                case BANK_CARD: return "银行卡";
                default: return "未知";
            }
        }
    }
}