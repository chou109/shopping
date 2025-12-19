package com.neuedu.system.util;

/**
 * 退款状态工具类
 */
public class RefundStatusUtil {

    private RefundStatusUtil() {}

    /**
     * 退款状态枚举
     */
    public enum RefundStatus {
        APPLYING(1, "申请中"),
        PROCESSING(2, "处理中"),
        SUCCESS(3, "退款成功"),
        FAILED(4, "退款失败"),
        REJECTED(5, "已拒绝");

        private final Integer code;
        private final String desc;

        RefundStatus(Integer code, String desc) {
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
        for (RefundStatus status : RefundStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }

    /**
     * 根据状态码获取状态枚举
     */
    public static RefundStatus getStatusByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundStatus status : RefundStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}